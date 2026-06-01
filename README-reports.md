# BPMS Reports — Руководство по проверке генерации отчётов

##  Описание
В системе реализована асинхронная генерация HTML-отчётов через REST API.

## API

| Метод | URL | Описание |
|------|-----|----------|
| POST | `/api/reports` | Создать отчёт |
| GET | `/api/reports/{id}` | Получить статус |
| GET | `/api/reports/{id}/content` | Получить HTML |

---

# Пошаговая инструкция (curl)

## 1. Авторизация

Все API защищены Spring Security, сначала нужно залогиниться:

```bash
curl -i -c cookies.txt \
  -X POST http://localhost:8080/login \
  -d "username=johndoe" \
  -d "password=efeuw*SQD322"

```
## 2. Создание отчёта
```
curl -i -b cookies.txt \
  -X POST http://localhost:8080/api/reports
```

## 3. Проверка статуса

```
curl -b cookies.txt \
   http://localhost:8080/api/reports/1
```
## 4. Получение HTML отчёта
```
curl -b cookies.txt \
    http://localhost:8080/api/reports/1/content \
    -o report.html
```

Лучше открыть в браузере для лучшего восприятия 

-> http://localhost:8080/api/reports/1/content