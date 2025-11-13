import { collection, doc, getDocs, getDoc, addDoc, updateDoc, deleteDoc, query, orderBy } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';
import { db, storage } from './firebase';
import { Jadid } from '../types';

const COLLECTION = 'jadids';

export const jadidService = {
  async getAll(): Promise<Jadid[]> {
    console.log('[jadidService] getAll() - Начало запроса');
    try {
      const q = query(collection(db, COLLECTION), orderBy('orderIndex'));
      console.log('[jadidService] getAll() - Запрос к Firestore выполнен');
      const snapshot = await getDocs(q);
      const result = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Jadid));
      console.log(`[jadidService] getAll() - Успешно получено ${result.length} записей`, result);
      return result;
    } catch (error) {
      console.error('[jadidService] getAll() - ОШИБКА:', error);
      throw error;
    }
  },

  async getById(id: string): Promise<Jadid | null> {
    console.log(`[jadidService] getById(${id}) - Начало запроса`);
    try {
      const docRef = doc(db, COLLECTION, id);
      const docSnap = await getDoc(docRef);
      const result = docSnap.exists() ? { id: docSnap.id, ...docSnap.data() } as Jadid : null;
      console.log(`[jadidService] getById(${id}) - ${result ? 'Найдено' : 'Не найдено'}`, result);
      return result;
    } catch (error) {
      console.error(`[jadidService] getById(${id}) - ОШИБКА:`, error);
      throw error;
    }
  },

  async create(data: Omit<Jadid, 'id'>, imageFile?: File): Promise<string> {
    console.log('[jadidService] create() - Начало создания', { data, hasImageFile: !!imageFile, imageFileName: imageFile?.name });
    try {
      let imageUrl = data.imageUrl;
      
      if (imageFile) {
        const storagePath = `images/jadids/${Date.now()}_${imageFile.name}`;
        console.log(`[jadidService] create() - Загрузка изображения в Storage: ${storagePath}`, {
          fileName: imageFile.name,
          fileSize: imageFile.size,
          fileType: imageFile.type
        });
        const storageRef = ref(storage, storagePath);
        console.log('[jadidService] create() - Вызов uploadBytes...');
        await uploadBytes(storageRef, imageFile);
        console.log('[jadidService] create() - uploadBytes успешно, получение URL...');
        imageUrl = await getDownloadURL(storageRef);
        console.log('[jadidService] create() - URL изображения получен:', imageUrl);
      }

      console.log('[jadidService] create() - Создание документа в Firestore...');
      const docRef = await addDoc(collection(db, COLLECTION), {
        ...data,
        imageUrl
      });
      console.log(`[jadidService] create() - Успешно создан документ с ID: ${docRef.id}`, {
        id: docRef.id,
        data: { ...data, imageUrl }
      });
      return docRef.id;
    } catch (error) {
      console.error('[jadidService] create() - ОШИБКА при создании:', error);
      console.error('[jadidService] create() - Детали ошибки:', {
        message: error instanceof Error ? error.message : String(error),
        stack: error instanceof Error ? error.stack : undefined,
        name: error instanceof Error ? error.name : undefined
      });
      throw error;
    }
  },

  async update(id: string, data: Partial<Omit<Jadid, 'id'>>, imageFile?: File): Promise<void> {
    console.log(`[jadidService] update(${id}) - Начало обновления`, { id, data, hasImageFile: !!imageFile, imageFileName: imageFile?.name });
    try {
      const updateData: Partial<Jadid> = { ...data };
      
      if (imageFile) {
        const storagePath = `images/jadids/${Date.now()}_${imageFile.name}`;
        console.log(`[jadidService] update(${id}) - Загрузка изображения в Storage: ${storagePath}`, {
          fileName: imageFile.name,
          fileSize: imageFile.size,
          fileType: imageFile.type
        });
        const storageRef = ref(storage, storagePath);
        console.log(`[jadidService] update(${id}) - Вызов uploadBytes...`);
        await uploadBytes(storageRef, imageFile);
        console.log(`[jadidService] update(${id}) - uploadBytes успешно, получение URL...`);
        updateData.imageUrl = await getDownloadURL(storageRef);
        console.log(`[jadidService] update(${id}) - URL изображения получен:`, updateData.imageUrl);
      }

      console.log(`[jadidService] update(${id}) - Обновление документа в Firestore...`);
      const docRef = doc(db, COLLECTION, id);
      await updateDoc(docRef, updateData);
      console.log(`[jadidService] update(${id}) - Успешно обновлен`, updateData);
    } catch (error) {
      console.error(`[jadidService] update(${id}) - ОШИБКА при обновлении:`, error);
      console.error(`[jadidService] update(${id}) - Детали ошибки:`, {
        message: error instanceof Error ? error.message : String(error),
        stack: error instanceof Error ? error.stack : undefined,
        name: error instanceof Error ? error.name : undefined
      });
      throw error;
    }
  },

  async delete(id: string): Promise<void> {
    console.log(`[jadidService] delete(${id}) - Начало удаления`);
    try {
      const jadid = await this.getById(id);
      if (jadid?.imageUrl) {
        try {
          console.log(`[jadidService] delete(${id}) - Удаление изображения из Storage:`, jadid.imageUrl);
          const imageRef = ref(storage, jadid.imageUrl);
          await deleteObject(imageRef);
          console.log(`[jadidService] delete(${id}) - Изображение успешно удалено из Storage`);
        } catch (error) {
          console.error(`[jadidService] delete(${id}) - Ошибка при удалении изображения:`, error);
        }
      }
      console.log(`[jadidService] delete(${id}) - Удаление документа из Firestore...`);
      const docRef = doc(db, COLLECTION, id);
      await deleteDoc(docRef);
      console.log(`[jadidService] delete(${id}) - Успешно удален`);
    } catch (error) {
      console.error(`[jadidService] delete(${id}) - ОШИБКА при удалении:`, error);
      throw error;
    }
  }
};
