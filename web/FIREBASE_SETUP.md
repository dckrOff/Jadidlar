# Firebase настройки для админ-панели

## 🚨 БЫСТРОЕ ИСПРАВЛЕНИЕ ОШИБОК

### Ошибка: "Missing or insufficient permissions"

Если вы получаете ошибку **"Missing or insufficient permissions"** при загрузке данных:

1. Откройте [Firebase Console](https://console.firebase.google.com/)
2. Выберите ваш проект **jadidlar-4fccd**
3. Перейдите в **Firestore Database** → **Rules**
4. Замените существующие правила на:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

5. Нажмите **"Опубликовать"** (Publish)
6. Перезагрузите ваше приложение

---

### Ошибка: "blocked by CORS policy"

Если вы получаете ошибку **"blocked by CORS policy"** при загрузке файлов:

1. Откройте [Firebase Console](https://console.firebase.google.com/)
2. Выберите ваш проект **jadidlar-4fccd**
3. Перейдите в **Storage** → **Rules**
4. Замените существующие правила на:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```

5. Нажмите **"Опубликовать"** (Publish)
6. Перезагрузите ваше приложение

✅ Проблема должна быть решена!

⚠️ Для продакшена смотрите раздел **"Security Rules для Storage"** ниже.

---

## Security Rules для Firestore

⚠️ **ВАЖНО**: Выберите один из вариантов ниже в зависимости от вашей ситуации.

### Вариант 1: Для разработки (НЕ для продакшена!)

Примените следующие правила в Firebase Console → Firestore Database → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

⚠️ Это разрешает всем читать и писать данные. Используйте только для разработки!

### Вариант 2: Для продакшена (Только чтение для публичных данных)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Коллекции для чтения (мобильное приложение)
    match /jadids/{document} {
      allow read: if true;
      allow write: if false; // Только через админ-панель с аутентификацией
    }
    
    match /books/{document} {
      allow read: if true;
      allow write: if false; // Только через админ-панель с аутентификацией
    }
    
    match /tests/{document} {
      allow read: if true;
      allow write: if false; // Только через админ-панель с аутентификацией
    }
    
    // Результаты тестов - пользователи могут создавать свои
    match /test_results/{document} {
      allow read: if true;
      allow create: if true;
      allow update, delete: if false;
    }
  }
}
```

### Вариант 3: С аутентификацией администратора

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Все могут читать
    match /{document=**} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.token.admin == true;
    }
    
    // Результаты тестов могут создавать все
    match /test_results/{document} {
      allow create: if true;
    }
  }
}
```

## Security Rules для Storage

⚠️ **ВАЖНО**: Выберите один из вариантов ниже в зависимости от вашей ситуации.

### Вариант 1: Для разработки (НЕ для продакшена!)

Примените следующие правила в Firebase Console → Storage → Rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```

⚠️ Это разрешает всем читать и писать файлы. Используйте только для разработки!

### Вариант 2: С аутентификацией (Рекомендуется)

Если вы добавили Firebase Authentication:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

### Вариант 3: Продакшн (С проверкой ролей)

Для продакшена с ролями администратора:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null && 
                      request.auth.token.admin == true;
    }
  }
}
```

## Структура коллекций

### jadids
- `id`: string (auto-generated)
- `name`: string
- `nameUz`: string
- `birthYear`: number
- `deathYear`: number
- `shortDescription`: string
- `fullDescription`: string
- `imageUrl`: string
- `orderIndex`: number

### books
- `id`: string (auto-generated)
- `title`: string
- `authorId`: string (reference to jadids)
- `authorName`: string
- `publishYear`: number
- `description`: string
- `coverImageUrl`: string
- `pdfUrl`: string
- `rating`: number (начальное значение: 0)
- `isFavorite`: boolean (начальное значение: false)
- `orderIndex`: number

### tests
- `id`: string (auto-generated)
- `title`: string
- `description`: string
- `questionCount`: number (автоматически)
- `timeLimit`: number (в минутах)
- `questions`: array of:
  - `id`: string
  - `questionText`: string
  - `answers`: array[4] of string
  - `correctAnswerIndex`: number (0-3)

### test_results
- `id`: string (auto-generated)
- `testId`: string
- `userId`: string
- `score`: number
- `totalQuestions`: number
- `timeSpent`: number (в секундах)
- `completedAt`: timestamp
- `answers`: array of:
  - `questionId`: string
  - `selectedAnswerIndex`: number
  - `isCorrect`: boolean

## Важные замечания

1. **Для продакшена**: После завершения разработки обязательно измените правила безопасности на более строгие (см. Вариант 2 или 3 выше). Текущие правила разработки позволяют кому угодно читать и изменять данные.

2. **Загрузка файлов**: После обновления правил Storage вы сможете загружать файлы из админ-панели. Для продакшена рекомендуется добавить Firebase Authentication.

3. **Индексы**: При первом запросе Firestore может потребовать создания индексов. Следуйте ссылкам в консоли браузера для их создания.

4. **CORS**: Если проблемы с CORS сохраняются после обновления правил Storage, настройте CORS:

   a. Создайте файл `cors.json`:
   ```json
   [
     {
       "origin": ["http://localhost:3000", "http://localhost:5173", "https://yourdomain.com"],
       "method": ["GET", "HEAD", "PUT", "POST", "DELETE"],
       "responseHeader": ["Content-Type"],
       "maxAgeSeconds": 3600
     }
   ]
   ```

   b. Установите Google Cloud SDK, если еще не установлен

   c. Примените CORS конфигурацию:
   ```bash
   gsutil cors set cors.json gs://jadidlar-4fccd.appspot.com
   ```
   
   ⚠️ **Обычно CORS настройка НЕ требуется** если правила Storage настроены правильно!
