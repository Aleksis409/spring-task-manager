API Endpoints
Аутентификация
- POST /api/auth/register     - Регистрация
- POST /api/auth/login        - Вход
- POST /api/auth/logout       - Выход
- POST /api/auth/refresh      - Обновление токена

Пользователи
- GET    /api/users/{id_user}  - Получить профиль {id_user}
- PUT    /api/users/{id_user}  - Обновить профиль {id_user}
- DELETE /api/users/{id_user}  - Удалить аккаунт {id_user}

Задачи
- GET    /api/tasks            - Получить все задачи
- GET    /api/tasks/{id}       - Получить задачу по ID
- POST   /api/tasks            - Создать задачу
- PUT    /api/tasks/{id}       - Обновить задачу
- DELETE /api/tasks/{id}       - Удалить задачу
- GET    /api/tasks/filter     - Фильтрация задач
- GET    /api/tasks/stats      - Статистика задач