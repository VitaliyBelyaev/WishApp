#!/usr/bin/env python3
"""
Замер памяти приложения на разном числе картинок в базе.

Наполняет базу через ImageMemorySeedTest, потом проходит по приложению как
пользователь (без тестового процесса) и параллельно снимает `dumpsys meminfo`.
На контрольных точках снимает heap dump и считает, сколько в нём byte[].

Стирает данные debug-приложения на указанном устройстве:

    ./gradlew :app:installDebug :app:installDebugAndroidTest
    scripts/profile_images_memory.py --serial emulator-5570 --images 50 200 500 --out /tmp/out
"""
import argparse
import json
import mmap
import os
import re
import struct
import subprocess
import sys
import threading
import time
import xml.etree.ElementTree as ET

PKG = "ru.vitaliy.belyaev.wishapp.debug"
RUNNER = f"{PKG}.test/androidx.test.runner.AndroidJUnitRunner"
SEED_CLASS = "ru.vitaliy.belyaev.wishapp.profiling.ImageMemorySeedTest"
ACTIVITY = "ru.vitaliy.belyaev.wishapp.ui.AppActivity"
SDK = os.environ.get("ANDROID_HOME", os.path.expanduser("~/Library/Android/sdk"))
ADB = f"{SDK}/platform-tools/adb"
HPROF_CONV = f"{SDK}/platform-tools/hprof-conv"
MB = 1024 * 1024


class Device:
    def __init__(self, serial):
        self.serial = serial

    def adb(self, *args, timeout=600, check=False):
        result = subprocess.run(
            [ADB, "-s", self.serial, *args], capture_output=True, text=True, timeout=timeout, errors="replace"
        )
        if check and result.returncode != 0:
            raise RuntimeError(f"adb {args}: {result.stdout} {result.stderr}")
        return result.stdout

    def shell(self, command, **kwargs):
        return self.adb("shell", command, **kwargs)

    def pid(self):
        return self.shell(f"pidof {PKG}").strip()

    def nodes(self):
        self.shell("uiautomator dump /data/local/tmp/ui.xml >/dev/null 2>&1")
        raw = self.shell("cat /data/local/tmp/ui.xml")
        try:
            root = ET.fromstring(raw[raw.index("<?xml"):])
        except (ValueError, ET.ParseError):
            return []
        result = []
        for node in root.iter("node"):
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds", "[0,0][0,0]")))
            result.append({
                "text": node.get("text", ""),
                "desc": node.get("content-desc", ""),
                "x": (x1 + x2) // 2,
                "y": (y1 + y2) // 2,
            })
        return result

    def find(self, predicate, attempts=10):
        for _ in range(attempts):
            found = [n for n in self.nodes() if predicate(n)]
            if found:
                return found
            time.sleep(1)
        return []

    def tap_label(self, label, prefix=False):
        def matches(node):
            for value in (node["text"], node["desc"]):
                if value == label or (prefix and value.startswith(label)):
                    return True
            return False

        found = self.find(matches)
        if not found:
            labels = [n["text"] or n["desc"] for n in self.nodes() if n["text"] or n["desc"]]
            raise RuntimeError(f"не нашёл {label!r} на экране: {labels[:40]}")
        self.shell(f"input tap {found[0]['x']} {found[0]['y']}")


class Sampler(threading.Thread):
    """Снимает meminfo в фоне. Heap Size в ART падает с задержкой, поэтому пики видны и при редких замерах."""

    def __init__(self, device):
        super().__init__(daemon=True)
        self.device = device
        self.step = "start"
        self.samples = []
        self.paused = False
        self.running = True

    def run(self):
        while self.running:
            if not self.paused:
                sample = read_meminfo(self.device)
                if sample:
                    sample["step"] = self.step
                    sample["t"] = time.time()
                    self.samples.append(sample)
            time.sleep(0.15)


def read_meminfo(device):
    output = device.shell(f"dumpsys meminfo {PKG}")

    def row(name):
        match = re.search(rf"^\s*{name}\s+(.*)$", output, re.M)
        return [int(v) for v in match.group(1).split() if v.isdigit()] if match else None

    dalvik = row("Dalvik Heap")
    native = row("Native Heap")
    total = re.search(r"TOTAL PSS:\s+(\d+)", output)
    if not dalvik or not native or not total:
        return None
    return {
        "java_size": dalvik[-3] / 1024,
        "java_alloc": dalvik[-2] / 1024,
        "native_alloc": native[-2] / 1024,
        "pss": int(total.group(1)) / 1024,
    }


def parse_hprof(path):
    """Считает объём объектов в дампе, обработанном hprof-conv."""
    sizes = {2: 0, 4: 1, 5: 2, 6: 4, 7: 8, 8: 1, 9: 2, 10: 4, 11: 8}
    stats = {"byte_total": 0, "byte_large_count": 0, "byte_large_total": 0, "other_arrays": 0, "instances": 0}
    with open(path, "rb") as file, mmap.mmap(file.fileno(), 0, access=mmap.ACCESS_READ) as data:
        pos = data.find(b"\0") + 1
        id_size = struct.unpack_from(">I", data, pos)[0]
        sizes[2] = id_size
        pos += 12
        end = len(data)
        while pos < end:
            tag = data[pos]
            length = struct.unpack_from(">I", data, pos + 5)[0]
            pos += 9
            if tag in (0x0C, 0x1C):
                walk_heap(data, pos, pos + length, id_size, sizes, stats)
            pos += length
    return stats


def walk_heap(data, pos, end, id_size, sizes, stats):
    fixed = {
        0xFF: id_size, 0x01: 2 * id_size, 0x02: id_size + 8, 0x03: id_size + 8, 0x04: id_size + 4,
        0x05: id_size, 0x06: id_size + 4, 0x07: id_size, 0x08: id_size + 8,
    }
    while pos < end:
        sub = data[pos]
        pos += 1
        if sub in fixed:
            pos += fixed[sub]
        elif sub == 0x20:
            pos += id_size + 4 + 6 * id_size + 4
            count = struct.unpack_from(">H", data, pos)[0]
            pos += 2
            for _ in range(count):
                pos += 2
                pos += 1 + sizes[data[pos]]
            count = struct.unpack_from(">H", data, pos)[0]
            pos += 2
            for _ in range(count):
                pos += id_size
                pos += 1 + sizes[data[pos]]
            count = struct.unpack_from(">H", data, pos)[0]
            pos += 2 + count * (id_size + 1)
        elif sub == 0x21:
            length = struct.unpack_from(">I", data, pos + id_size + 4 + id_size)[0]
            stats["instances"] += length
            pos += id_size + 4 + id_size + 4 + length
        elif sub == 0x22:
            count = struct.unpack_from(">I", data, pos + id_size + 4)[0]
            stats["other_arrays"] += count * id_size
            pos += id_size + 4 + 4 + id_size + count * id_size
        elif sub == 0x23:
            count, kind = struct.unpack_from(">IB", data, pos + id_size + 4)
            size = count * sizes[kind]
            if kind == 8:
                stats["byte_total"] += size
                if size >= 50 * 1024:
                    stats["byte_large_count"] += 1
                    stats["byte_large_total"] += size
            else:
                stats["other_arrays"] += size
            pos += id_size + 4 + 4 + 1 + size
        else:
            raise ValueError(f"неизвестная запись hprof 0x{sub:02x} в позиции {pos}")


def heap_dump(device, sampler, out_dir, name):
    sampler.paused = True
    time.sleep(0.5)
    remote = f"/data/local/tmp/{name}.hprof"
    raw = os.path.join(out_dir, f"{name}.raw.hprof")
    converted = os.path.join(out_dir, f"{name}.hprof")
    device.shell(f"am dumpheap -g {PKG} {remote}")
    time.sleep(2)
    device.adb("pull", remote, raw, check=True)
    device.shell(f"rm {remote}")
    subprocess.run([HPROF_CONV, raw, converted], check=True)
    stats = parse_hprof(converted)
    os.remove(raw)
    os.remove(converted)
    sampler.paused = False
    return {key: round(value / MB, 1) if "count" not in key else value for key, value in stats.items()}


def seed(device, images):
    device.shell(f"am force-stop {PKG}")
    device.shell(f"pm clear {PKG}")
    output = device.shell(f"am instrument -w -e images {images} -e class {SEED_CLASS}#seedDatabase {RUNNER}")
    if "OK (" not in output:
        raise RuntimeError(f"seed failed: {output}")
    device.shell(f"am force-stop {PKG}")


def ensure_gallery(device):
    photos = device.shell(
        "content query --uri content://media/external/images/media --projection _display_name"
    )
    if "profiling_0.jpg" in photos:
        return
    output = device.shell(f"am instrument -w -e gallery 1 -e class {SEED_CLASS}#seedGallery {RUNNER}")
    if "OK (" not in output:
        raise RuntimeError(f"gallery seed failed: {output}")


def run_scenario(device, images, out_dir, dumps):
    seed(device, images)
    device.adb("logcat", "-c")
    sampler = Sampler(device)
    result = {"images": images, "dumps": {}, "crashed_at": None}

    def alive(step):
        if not device.pid():
            result["crashed_at"] = step
            return False
        return True

    def begin(step):
        sampler.step = step
        print(f"  [{images}] {step}", flush=True)

    try:
        begin("cold_start")
        device.shell(f"am start -W -n {PKG}/{ACTIVITY}")
        time.sleep(2)
        sampler.start()
        time.sleep(10)
        if not alive("cold_start"):
            return finish(device, sampler, result)
        if dumps:
            result["dumps"]["idle_list"] = heap_dump(device, sampler, out_dir, f"n{images}_idle_list")

        begin("scroll_list")
        for _ in range(6):
            device.shell("input swipe 540 1800 540 500 200")
            time.sleep(0.7)
        if not alive("scroll_list"):
            return finish(device, sampler, result)

        begin("filter_switch")
        device.tap_label("Navigation menu")
        time.sleep(1)
        device.tap_label("Completed wishes")
        time.sleep(4)
        device.tap_label("Navigation menu")
        time.sleep(1)
        device.tap_label("Current wishes")
        time.sleep(6)
        if not alive("filter_switch"):
            return finish(device, sampler, result)

        begin("open_wish")
        device.tap_label("Желание", prefix=True)
        time.sleep(3)
        if not alive("open_wish"):
            return finish(device, sampler, result)

        begin("add_5_photos")
        device.tap_label("Add image")
        time.sleep(3)
        photos = device.find(lambda n: n["desc"].startswith("Photo"))
        if len(photos) < 5:
            raise RuntimeError(f"в пикере мало фото: {[n['text'] or n['desc'] for n in device.nodes()][:40]}")
        for photo in photos[:5]:
            device.shell(f"input tap {photo['x']} {photo['y']}")
            time.sleep(0.3)
        device.tap_label("Add", prefix=True)
        time.sleep(12)
        if not alive("add_5_photos"):
            return finish(device, sampler, result)

        begin("back_to_list")
        device.shell("input keyevent 4")
        time.sleep(2)
        if not device.find(lambda n: n["desc"] == "Add wish", attempts=2):
            device.shell("input keyevent 4")
        if not device.find(lambda n: n["desc"] == "Add wish"):
            raise RuntimeError("не вернулись на список")
        time.sleep(5)
        if not alive("back_to_list"):
            return finish(device, sampler, result)

        begin("background")
        device.shell("input keyevent 3")
        time.sleep(6)
        if not alive("background"):
            return finish(device, sampler, result)
        if dumps:
            result["dumps"]["background"] = heap_dump(device, sampler, out_dir, f"n{images}_background")
    except RuntimeError as error:
        # На экране нет ожидаемого элемента: обычно приложение уже упало и открылся лаунчер
        result["crashed_at"] = sampler.step
        result["error"] = str(error)[:200]
    finally:
        sampler.running = False
    return finish(device, sampler, result)


def finish(device, sampler, result):
    sampler.running = False
    log = device.adb("logcat", "-d", "-v", "brief")
    result["oom_lines"] = [
        line.strip()[:220] for line in log.splitlines()
        if "OutOfMemoryError" in line and ("Throwing" in line or "FATAL" in line or "E/" in line)
    ][:12]
    result["oom_count"] = sum("Throwing OutOfMemoryError" in line for line in log.splitlines())
    result["fatal"] = [line.strip()[:220] for line in log.splitlines() if "FATAL EXCEPTION" in line][:3]
    result["swallowed_sqlite_oom"] = sum(
        "launchObservingWishes" in line and "could not create new byte" in line for line in log.splitlines()
    )
    steps = {}
    for sample in sampler.samples:
        step = steps.setdefault(sample["step"], {"java_size": 0, "java_alloc": 0, "native_alloc": 0, "pss": 0, "n": 0})
        step["n"] += 1
        for key in ("java_size", "java_alloc", "native_alloc", "pss"):
            step[key] = round(max(step[key], sample[key]), 1)
    result["steps"] = steps
    result["samples"] = sampler.samples
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", required=True)
    parser.add_argument("--images", type=int, nargs="+", required=True)
    parser.add_argument("--out", required=True)
    parser.add_argument("--label", default="run")
    parser.add_argument("--no-dumps", action="store_true")
    args = parser.parse_args()
    os.makedirs(args.out, exist_ok=True)
    device = Device(args.serial)
    print("heapgrowthlimit", device.shell("getprop dalvik.vm.heapgrowthlimit").strip(), flush=True)
    ensure_gallery(device)
    results = []
    for images in args.images:
        try:
            result = run_scenario(device, images, args.out, not args.no_dumps)
        except Exception as error:  # сценарий мог упасть из-за того, что приложение не отвечает
            result = {"images": images, "error": str(error), "alive": bool(device.pid())}
            print("  error:", error, file=sys.stderr, flush=True)
        results.append(result)
        with open(os.path.join(args.out, f"{args.label}_n{images}.json"), "w") as file:
            json.dump(result, file, ensure_ascii=False, indent=1)
        printable = {k: v for k, v in result.items() if k != "samples"}
        print(json.dumps(printable, ensure_ascii=False), flush=True)


if __name__ == "__main__":
    main()
