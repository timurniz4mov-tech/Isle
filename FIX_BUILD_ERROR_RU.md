# Исправление ошибки сборки ISLE Survival

В этом обновлении исправлены две причины падения `:compileJava`:

1. `HurtByTargetGoal` импортируется из правильного пакета Minecraft Forge 1.20.1:
   `net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal`.
2. Для установки листвы передавался `BlockState` в метод, который принимал только `Block`.
   Добавлен метод `setState(ServerLevel, BlockPos, BlockState)`, и листва устанавливается через него.

## Как применить

- Для полной замены проекта распакуйте архив с исправленным проектом и загрузите содержимое в репозиторий GitHub `Isle`, заменив старые файлы.
- Для точечной замены используйте архив `isle-survival-error-fix-only.zip`: сохраните структуру папок и замените два Java-файла в репозитории.
- Затем откройте GitHub → Actions и запустите workflow `Build ISLE Survival` (или отправьте commit в `main`).

Эти изменения адресуют ошибки из лога `:compileJava`. Полноценная сборка Forge в данной среде не запускалась, поэтому успешность следующего GitHub Actions run ещё нужно подтвердить.
