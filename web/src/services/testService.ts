import { collection, doc, getDocs, getDoc, addDoc, updateDoc, deleteDoc, query, orderBy } from 'firebase/firestore';
import { db } from './firebase';
import { Test, TestResult } from '../types';

const COLLECTION = 'tests';
const RESULTS_COLLECTION = 'test_results';

export const testService = {
  async getAll(): Promise<Test[]> {
    console.log('[testService] getAll() - Начало запроса');
    try {
      const q = query(collection(db, COLLECTION), orderBy('title'));
      const snapshot = await getDocs(q);
      const result = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Test));
      console.log(`[testService] getAll() - Успешно получено ${result.length} записей`, result);
      return result;
    } catch (error) {
      console.error('[testService] getAll() - ОШИБКА:', error);
      throw error;
    }
  },

  async getById(id: string): Promise<Test | null> {
    console.log(`[testService] getById(${id}) - Начало запроса`);
    try {
      const docRef = doc(db, COLLECTION, id);
      const docSnap = await getDoc(docRef);
      const result = docSnap.exists() ? { id: docSnap.id, ...docSnap.data() } as Test : null;
      console.log(`[testService] getById(${id}) - ${result ? 'Найдено' : 'Не найдено'}`, result);
      return result;
    } catch (error) {
      console.error(`[testService] getById(${id}) - ОШИБКА:`, error);
      throw error;
    }
  },

  async create(data: Omit<Test, 'id'>): Promise<string> {
    console.log('[testService] create() - Начало создания', { 
      title: data.title, 
      questionCount: data.questions.length 
    });
    try {
      const docRef = await addDoc(collection(db, COLLECTION), {
        ...data,
        questionCount: data.questions.length
      });
      console.log(`[testService] create() - Успешно создан документ с ID: ${docRef.id}`);
      return docRef.id;
    } catch (error) {
      console.error('[testService] create() - ОШИБКА при создании:', error);
      throw error;
    }
  },

  async update(id: string, data: Partial<Omit<Test, 'id'>>): Promise<void> {
    console.log(`[testService] update(${id}) - Начало обновления`, { id, data });
    try {
      const updateData = { ...data };
      if (data.questions) {
        updateData.questionCount = data.questions.length;
      }
      const docRef = doc(db, COLLECTION, id);
      await updateDoc(docRef, updateData);
      console.log(`[testService] update(${id}) - Успешно обновлен`);
    } catch (error) {
      console.error(`[testService] update(${id}) - ОШИБКА при обновлении:`, error);
      throw error;
    }
  },

  async delete(id: string): Promise<void> {
    console.log(`[testService] delete(${id}) - Начало удаления`);
    try {
      const docRef = doc(db, COLLECTION, id);
      await deleteDoc(docRef);
      console.log(`[testService] delete(${id}) - Успешно удален`);
    } catch (error) {
      console.error(`[testService] delete(${id}) - ОШИБКА при удалении:`, error);
      throw error;
    }
  },

  async getResults(): Promise<TestResult[]> {
    console.log('[testService] getResults() - Начало запроса');
    try {
      const q = query(collection(db, RESULTS_COLLECTION), orderBy('completedAt', 'desc'));
      const snapshot = await getDocs(q);
      const result = snapshot.docs.map(doc => {
        const data = doc.data();
        return {
          id: doc.id,
          ...data,
          completedAt: data.completedAt?.toDate()
        } as TestResult;
      });
      console.log(`[testService] getResults() - Успешно получено ${result.length} результатов`, result);
      return result;
    } catch (error) {
      console.error('[testService] getResults() - ОШИБКА:', error);
      throw error;
    }
  }
};
