import { collection, doc, getDocs, getDoc, addDoc, updateDoc, deleteDoc, query, orderBy } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';
import { db, storage } from './firebase';
import { Book } from '../types';

const COLLECTION = 'books';

export const bookService = {
  async getAll(): Promise<Book[]> {
    console.log('[bookService] getAll() - Начало запроса');
    try {
      const q = query(collection(db, COLLECTION), orderBy('orderIndex'));
      const snapshot = await getDocs(q);
      const result = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Book));
      console.log(`[bookService] getAll() - Успешно получено ${result.length} записей`, result);
      return result;
    } catch (error) {
      console.error('[bookService] getAll() - ОШИБКА:', error);
      throw error;
    }
  },

  async getById(id: string): Promise<Book | null> {
    console.log(`[bookService] getById(${id}) - Начало запроса`);
    try {
      const docRef = doc(db, COLLECTION, id);
      const docSnap = await getDoc(docRef);
      const result = docSnap.exists() ? { id: docSnap.id, ...docSnap.data() } as Book : null;
      console.log(`[bookService] getById(${id}) - ${result ? 'Найдено' : 'Не найдено'}`, result);
      return result;
    } catch (error) {
      console.error(`[bookService] getById(${id}) - ОШИБКА:`, error);
      throw error;
    }
  },

  async create(data: Omit<Book, 'id'>, coverFile?: File, pdfFile?: File): Promise<string> {
    console.log('[bookService] create() - Начало создания', { 
      data, 
      hasCoverFile: !!coverFile, 
      coverFileName: coverFile?.name,
      hasPdfFile: !!pdfFile,
      pdfFileName: pdfFile?.name 
    });
    try {
      let coverImageUrl = data.coverImageUrl;
      let pdfUrl = data.pdfUrl;
      
      if (coverFile) {
        const storagePath = `images/books/${Date.now()}_${coverFile.name}`;
        console.log(`[bookService] create() - Загрузка обложки в Storage: ${storagePath}`, {
          fileName: coverFile.name,
          fileSize: coverFile.size,
          fileType: coverFile.type
        });
        const storageRef = ref(storage, storagePath);
        await uploadBytes(storageRef, coverFile);
        coverImageUrl = await getDownloadURL(storageRef);
        console.log('[bookService] create() - URL обложки получен:', coverImageUrl);
      }

      if (pdfFile) {
        const storagePath = `books/${Date.now()}_${pdfFile.name}`;
        console.log(`[bookService] create() - Загрузка PDF в Storage: ${storagePath}`, {
          fileName: pdfFile.name,
          fileSize: pdfFile.size,
          fileType: pdfFile.type
        });
        const storageRef = ref(storage, storagePath);
        await uploadBytes(storageRef, pdfFile);
        pdfUrl = await getDownloadURL(storageRef);
        console.log('[bookService] create() - URL PDF получен:', pdfUrl);
      }

      console.log('[bookService] create() - Создание документа в Firestore...');
      const docRef = await addDoc(collection(db, COLLECTION), {
        ...data,
        coverImageUrl,
        pdfUrl,
        rating: 0,
        isFavorite: false
      });
      console.log(`[bookService] create() - Успешно создан документ с ID: ${docRef.id}`);
      return docRef.id;
    } catch (error) {
      console.error('[bookService] create() - ОШИБКА при создании:', error);
      console.error('[bookService] create() - Детали ошибки:', {
        message: error instanceof Error ? error.message : String(error),
        stack: error instanceof Error ? error.stack : undefined
      });
      throw error;
    }
  },

  async update(id: string, data: Partial<Omit<Book, 'id'>>, coverFile?: File, pdfFile?: File): Promise<void> {
    console.log(`[bookService] update(${id}) - Начало обновления`, { 
      id, 
      data, 
      hasCoverFile: !!coverFile,
      hasPdfFile: !!pdfFile 
    });
    try {
      const updateData: Partial<Book> = { ...data };
      
      if (coverFile) {
        const storagePath = `images/books/${Date.now()}_${coverFile.name}`;
        console.log(`[bookService] update(${id}) - Загрузка обложки в Storage: ${storagePath}`);
        const storageRef = ref(storage, storagePath);
        await uploadBytes(storageRef, coverFile);
        updateData.coverImageUrl = await getDownloadURL(storageRef);
        console.log(`[bookService] update(${id}) - URL обложки получен:`, updateData.coverImageUrl);
      }

      if (pdfFile) {
        const storagePath = `books/${Date.now()}_${pdfFile.name}`;
        console.log(`[bookService] update(${id}) - Загрузка PDF в Storage: ${storagePath}`);
        const storageRef = ref(storage, storagePath);
        await uploadBytes(storageRef, pdfFile);
        updateData.pdfUrl = await getDownloadURL(storageRef);
        console.log(`[bookService] update(${id}) - URL PDF получен:`, updateData.pdfUrl);
      }

      console.log(`[bookService] update(${id}) - Обновление документа в Firestore...`);
      const docRef = doc(db, COLLECTION, id);
      await updateDoc(docRef, updateData);
      console.log(`[bookService] update(${id}) - Успешно обновлен`);
    } catch (error) {
      console.error(`[bookService] update(${id}) - ОШИБКА при обновлении:`, error);
      throw error;
    }
  },

  async delete(id: string): Promise<void> {
    console.log(`[bookService] delete(${id}) - Начало удаления`);
    try {
      const book = await this.getById(id);
      if (book) {
        try {
          if (book.coverImageUrl) {
            console.log(`[bookService] delete(${id}) - Удаление обложки из Storage:`, book.coverImageUrl);
            const imageRef = ref(storage, book.coverImageUrl);
            await deleteObject(imageRef);
            console.log(`[bookService] delete(${id}) - Обложка успешно удалена`);
          }
          if (book.pdfUrl) {
            console.log(`[bookService] delete(${id}) - Удаление PDF из Storage:`, book.pdfUrl);
            const pdfRef = ref(storage, book.pdfUrl);
            await deleteObject(pdfRef);
            console.log(`[bookService] delete(${id}) - PDF успешно удален`);
          }
        } catch (error) {
          console.error(`[bookService] delete(${id}) - Ошибка при удалении файлов:`, error);
        }
      }
      console.log(`[bookService] delete(${id}) - Удаление документа из Firestore...`);
      const docRef = doc(db, COLLECTION, id);
      await deleteDoc(docRef);
      console.log(`[bookService] delete(${id}) - Успешно удален`);
    } catch (error) {
      console.error(`[bookService] delete(${id}) - ОШИБКА при удалении:`, error);
      throw error;
    }
  }
};
