#!/bin/bash
# Подключает git-хуки проекта из scripts/hooks (сейчас это pre-commit).
# Настройка общая для всех worktree этого репозитория.
cd "$(git rev-parse --show-toplevel)" || exit 1
git config core.hooksPath scripts/hooks
echo "core.hooksPath = scripts/hooks"
