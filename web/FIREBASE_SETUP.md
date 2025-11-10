# Firebase настройки для админ-панели

## Security Rules для Firestore

Примените следующие правила в Firebase Console → Firestore Database → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Коллекции для чтения (мобильное приложение)
    match /jadids/{document} {
      allow read: if true;
      allow write: if false; // Только через админ-панель
    }
    
    match /books/{document} {
      allow read: if true;
      allow write: if false; // Только через админ-панель
    }
    
    match /tests/{document} {
      allow read: if true;
      allow write: if false; // Только через админ-панель
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

## Security Rules для Storage

Примените следующие правила в Firebase Console → Storage → Rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Изображения джадидов
    match /images/jadids/{allPaths=**} {
      allow read: if true;
      allow write: if false; // Только через админ SDK
    }
    
    // Обложки книг
    match /images/books/{allPaths=**} {
      allow read: if true;
      allow write: if false; // Только через админ SDK
    }
    
    // PDF файлы книг
    match /books/{allPaths=**} {
      allow read: if true;
      allow write: if false; // Только через админ SDK
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

1. **Для продакшена**: Текущая конфигурация позволяет всем читать данные, но писать никому. Для реальной админ-панели добавьте Firebase Authentication и проверку ролей администратора.

2. **Загрузка файлов**: Storage rules настроены на "только чтение". Для работы загрузки файлов из веб-панели необходимо:
   - Либо использовать Firebase Admin SDK на бэкенде
   - Либо добавить аутентификацию и разрешить запись для админов

3. **Индексы**: При первом запросе Firestore может потребовать создания индексов. Следуйте ссылкам в консоли браузера для их создания.

4. **CORS**: Для работы с Storage из веб-приложения может потребоваться настроить CORS:

```json
[
  {
    "origin": ["http://localhost:3000", "https://yourdomain.com"],
    "method": ["GET", "PUT", "POST", "DELETE"],
    "maxAgeSeconds": 3600
  }
]
```

Сохраните в `cors.json` и примените:
```bash
gsutil cors set cors.json gs://your-bucket-name.appspot.com
```
