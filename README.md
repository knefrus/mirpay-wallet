# Mir Pay Wallet

Экран оплаты Mir Pay для часов на Wear OS.

Пока идёт оплата, экран не гаснет. От карты расходятся волны, по краю тает таймер.
После успешной оплаты — галочка и возврат на циферблат.

## Возможности

- Дизайны карт российских банков, включая Т-Банк Мир Supreme
- Автоопределение успешной оплаты по экрану и уведомлениям Mir Pay
- Запуск с циферблата через усложнение
- Таймер и вибрация настраиваются

## Установка

Нужны: часы на Wear OS, ПК с [ADB](https://developer.android.com/tools/releases/platform-tools) и установленный на часах Mir Pay с добавленной картой.

1. Скачайте `MirPayWallet.apk` из [Releases](../../releases).
2. На часах включите «Отладка ADB» и «Отладка по Wi-Fi».
3. Выполните на ПК:

```sh
adb connect IP:PORT
adb install MirPayWallet.apk
```

4. Включите службу, которая определяет оплату:

```sh
adb shell settings put secure enabled_accessibility_services me.thedise.mirpayinvoke/me.thedise.mirpayinvoke.main.MainAccessibilityService
adb shell settings put secure accessibility_enabled 1
```

5. Добавьте усложнение «Mir Pay Wallet» на циферблат.

Настройки открываются свайпом влево. Там же есть тест анимации и диагностика.

## Сборка

```sh
./gradlew assembleRelease
```

GitHub Actions собирает APK при каждом коммите в `main` и прикрепляет его к каждому релизу.

## Благодарности

Форк [the-dise/Mir-Pay-Wallet](https://github.com/the-dise/Mir-Pay-Wallet). Идея — Gorban и [MirPayTimer](https://github.com/s44khin/MirPayTimer).

## Лицензия

[GPL-3.0](LICENSE). Проект не связан с НСПК и банками. Названия и дизайны карт принадлежат их владельцам.
