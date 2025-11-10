import { collection, doc, getDocs, getDoc, addDoc, updateDoc, deleteDoc, query, orderBy } from 'firebase/firestore';
import { db } from './firebase';
import { Test, TestResult } from '../types';

const COLLECTION = 'tests';
const RESULTS_COLLECTION = 'test_results';

export const testService = {
  async getAll(): Promise<Test[]> {
    const q = query(collection(db, COLLECTION), orderBy('title'));
    const snapshot = await getDocs(q);
    return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Test));
  },

  async getById(id: string): Promise<Test | null> {
    const docRef = doc(db, COLLECTION, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? { id: docSnap.id, ...docSnap.data() } as Test : null;
  },

  async create(data: Omit<Test, 'id'>): Promise<string> {
    const docRef = await addDoc(collection(db, COLLECTION), {
      ...data,
      questionCount: data.questions.length
    });
    return docRef.id;
  },

  async update(id: string, data: Partial<Omit<Test, 'id'>>): Promise<void> {
    const updateData = { ...data };
    if (data.questions) {
      updateData.questionCount = data.questions.length;
    }
    const docRef = doc(db, COLLECTION, id);
    await updateDoc(docRef, updateData);
  },

  async delete(id: string): Promise<void> {
    const docRef = doc(db, COLLECTION, id);
    await deleteDoc(docRef);
  },

  async getResults(): Promise<TestResult[]> {
    const q = query(collection(db, RESULTS_COLLECTION), orderBy('completedAt', 'desc'));
    const snapshot = await getDocs(q);
    return snapshot.docs.map(doc => {
      const data = doc.data();
      return {
        id: doc.id,
        ...data,
        completedAt: data.completedAt?.toDate()
      } as TestResult;
    });
  }
};
