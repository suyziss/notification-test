# Notification Timer

Небольшое Android-приложение для запуска таймера и получения системного уведомления по его завершении.

## Требования
- Android 10 (API 29) и новее
- Для Android 12+ требуется разрешение на точные будильники
- Для Android 13+ требуется разрешение на уведомления

## Сборка
GitHub Actions автоматически собирает Debug APK при push в main/master или вручную через Actions → Android APK → Run workflow.

APK находится в Artifacts сборки под именем `notification-timer-debug`.

Пакет: `com.suyziss.notificationtimer`
