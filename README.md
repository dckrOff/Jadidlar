# Jadidlar - Мобильное приложение

Электронная библиотека, посвященная джадидам — представителям прогрессивного просветительского движения в Средней Азии.

## 📱 Технологии

- **Платформа**: Android
- **Язык**: Kotlin
- **Архитектура**: MVVM + Clean Architecture
- **Backend**: Firebase (Firestore, Storage, Analytics, Crashlytics)
- **UI**: Material Design 3, ViewBinding
- **Библиотеки**:
  - Navigation Component
  - Lifecycle (ViewModel, LiveData)
  - Kotlin Coroutines
  - Glide (загрузка изображений)
  - PDF Viewer (afreakyelf/Pdf-Viewer)

## 🏗️ Структура проекта

```
uz.dckroff.jadidlar/
├── JadidlarApplication.kt
├── data/
│   ├── models/          # Jadid, Book, Test, Question, TestResult, Answer
│   ├── repository/      # JadidRepository, BookRepository, TestRepository
│   └── firebase/        # FirebaseManager
├── ui/
│   ├── MainActivity.kt
│   ├── home/           # HomeFragment + ViewModel
│   ├── jadidlar/       # JadidlarFragment, JadidDetailFragment + ViewModels
│   ├── books/          # BooksFragment, BookDetailFragment, BookReaderFragment + ViewModels
│   ├── quiz/           # QuizListFragment, QuizSessionFragment, QuizResultsFragment + ViewModels
│   └── adapters/       # JadidAdapter, BookAdapter, QuizAdapter, QuestionResultAdapter
└── utils/              # FavoritesManager, ReadingProgressManager, AnalyticsHelper, etc.
```

## 📋 Реализованные модули

### ✅ Модуль 1: Настройка проекта
- Gradle dependencies (Firebase, Navigation, Lifecycle, Coroutines, Glide, PDF Viewer)
- Firebase интеграция с offline persistence
- ViewBinding
- ProGuard rules
- Navigation Component

### ✅ Модуль 2: Bottom Navigation и главный экран
- MainActivity с Bottom Navigation (3 раздела)
- HomeFragment с списками джадидов и топ книг
- Адаптеры: JadidAdapter, BookAdapter

### ✅ Модуль 3: Экраны Джадидов
- JadidlarFragment - сетка всех джадидов (3 колонки)
- JadidDetailFragment - детальная информация с книгами автора
- Expand/collapse описания
- Избранное (SharedPreferences)

### ✅ Модуль 4: Экраны Литературы
- BooksFragment - список всех книг с поиском и фильтрами
- BookDetailFragment - детальная информация о книге
- BookReaderFragment - PDF reader с навигацией и сохранением прогресса
- Функции: поиск, сортировка, избранное, скачивание

### ✅ Модуль 5: Экраны Тестов
- QuizListFragment - список всех тестов
- QuizSessionFragment - прохождение теста с таймером
- QuizResultsFragment - результаты с детальным разбором
- Сохранение результатов в Firebase

### ✅ Модуль 6: Дополнительные функции
- Система избранного (SharedPreferences)
- Скачивание PDF (DownloadManager)
- Функция "Поделиться"
- Поиск по книгам

### ✅ Модуль 7: Оптимизация
- Glide кэширование изображений
- Firebase offline persistence
- Error/Loading/Empty states

### ✅ Модуль 9: Аналитика
- Firebase Analytics (8 событий)
- Firebase Crashlytics

### ✅ Модуль 11: Обработка ошибок
- Network errors
- Firebase errors
- Validation errors
- Crashlytics логирование

## 🚀 Запуск проекта

### 1. Firebase Configuration

Замените файл `app/google-services.json` настоящим из вашей Firebase Console:

1. Перейдите в [Firebase Console](https://console.firebase.google.com/)
2. Создайте новый проект или выберите существующий
3. Добавьте Android приложение с package name: `uz.dckroff.jadidlar`
4. Скачайте `google-services.json`
5. Поместите в `app/google-services.json`

### 2. Firebase Database Structure

Создайте следующие коллекции в Firestore:

#### Коллекция `jadids`:
```json
{
  "id": "jadid_001",
  "name": "Mahmudxo'ja Behbudiy",
  "nameUz": "Махмудхўжа Беҳбудий",
  "birthYear": 1875,
  "deathYear": 1919,
  "shortDescription": "O'zbek jadidchilik harakatining yirik namoyandasi...",
  "fullDescription": "Batafsil biografiya...",
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "orderIndex": 1
}
```

#### Коллекция `books`:
```json
{
  "id": "book_001",
  "title": "Yoshlarga murojaat",
  "authorId": "jadid_001",
  "authorName": "Mahmudxo'ja Behbudiy",
  "publishYear": 1917,
  "description": "Kitob tavsifi...",
  "coverImageUrl": "https://firebasestorage.googleapis.com/...",
  "pdfUrl": "https://firebasestorage.googleapis.com/...",
  "rating": 150,
  "isFavorite": false,
  "orderIndex": 1
}
```

#### Коллекция `tests`:
```json
{
  "id": "test_001",
  "title": "Mahmudxo'ja Behbudiy. Tanlangan asarlar",
  "description": "Test tavsifi...",
  "questionCount": 10,
  "timeLimit": 15,
  "questions": [
    {
      "id": "q1",
      "questionText": "Savol matni?",
      "answers": ["Javob 1", "Javob 2", "Javob 3", "Javob 4"],
      "correctAnswerIndex": 0
    }
  ]
}
```

### 3. Firebase Security Rules

#### Firestore Rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /jadids/{document} {
      allow read: if true;
      allow write: if false;
    }
    match /books/{document} {
      allow read: if true;
      allow write: if false;
    }
    match /tests/{document} {
      allow read: if true;
      allow write: if false;
    }
    match /test_results/{document} {
      allow read: if true;
      allow create: if true;
      allow update, delete: if false;
    }
  }
}
```

#### Storage Rules:
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /images/{allPaths=**} {
      allow read: if true;
      allow write: if false;
    }
    match /books/{allPaths=**} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

### 4. Сборка проекта

```bash
# Синхронизация Gradle
./gradlew clean build

# Запуск на эмуляторе/устройстве
./gradlew installDebug
```

## 📊 Статистика проекта

- **Всего Kotlin файлов**: 39
- **Data models**: 6
- **Repositories**: 3
- **Fragments**: 9
- **ViewModels**: 9
- **Adapters**: 4
- **Utils**: 6
- **Layouts**: 15+

## 🎯 Особенности реализации

### PDF Reader
- Использует библиотеку `afreakyelf/Pdf-Viewer`
- Сохранение прогресса чтения в SharedPreferences
- Навигация: кнопки, слайдер, свайпы
- Zoom support

### Тесты с таймером
- CountDownTimer для обратного отсчета
- Сохранение ответов между вопросами
- Автоматическое завершение при окончании времени
- Детальный разбор результатов

### Избранное
- SharedPreferences для локального хранения
- Отдельные Set для джадидов и книг
- Toggle функционал

### Analytics
- 8 событий Firebase Analytics
- Автоматическое логирование действий пользователя

## 📝 TODO для продакшена

1. ✅ Заменить `google-services.json` настоящим
2. ⚠️ Создать локализацию:
   - `res/values-ru/strings.xml` (русский)
   - `res/values-en/strings.xml` (английский)
3. ⚠️ Заполнить Firebase тестовыми данными
4. ⚠️ Протестировать на реальных устройствах
5. ⚠️ Добавить иконки для меню (сейчас используются placeholder)
6. ⚠️ Настроить Crashlytics в Firebase Console
7. ⚠️ Проверить ProGuard rules перед release build

## 📞 Контакты

Package name: `uz.dckroff.jadidlar`
Min SDK: 24 (Android 7.0)
Target SDK: 34 (Android 14)

---

**Статус**: ✅ Все модули реализованы согласно ТЗ
