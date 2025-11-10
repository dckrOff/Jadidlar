import { collection, doc, getDocs, getDoc, addDoc, updateDoc, deleteDoc, query, orderBy } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';
import { db, storage } from './firebase';
import { Book } from '../types';

const COLLECTION = 'books';

export const bookService = {
  async getAll(): Promise<Book[]> {
    const q = query(collection(db, COLLECTION), orderBy('orderIndex'));
    const snapshot = await getDocs(q);
    return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Book));
  },

  async getById(id: string): Promise<Book | null> {
    const docRef = doc(db, COLLECTION, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? { id: docSnap.id, ...docSnap.data() } as Book : null;
  },

  async create(data: Omit<Book, 'id'>, coverFile?: File, pdfFile?: File): Promise<string> {
    let coverImageUrl = data.coverImageUrl;
    let pdfUrl = data.pdfUrl;
    
    if (coverFile) {
      const storageRef = ref(storage, `images/books/${Date.now()}_${coverFile.name}`);
      await uploadBytes(storageRef, coverFile);
      coverImageUrl = await getDownloadURL(storageRef);
    }

    if (pdfFile) {
      const storageRef = ref(storage, `books/${Date.now()}_${pdfFile.name}`);
      await uploadBytes(storageRef, pdfFile);
      pdfUrl = await getDownloadURL(storageRef);
    }

    const docRef = await addDoc(collection(db, COLLECTION), {
      ...data,
      coverImageUrl,
      pdfUrl,
      rating: 0,
      isFavorite: false
    });
    return docRef.id;
  },

  async update(id: string, data: Partial<Omit<Book, 'id'>>, coverFile?: File, pdfFile?: File): Promise<void> {
    const updateData: Partial<Book> = { ...data };
    
    if (coverFile) {
      const storageRef = ref(storage, `images/books/${Date.now()}_${coverFile.name}`);
      await uploadBytes(storageRef, coverFile);
      updateData.coverImageUrl = await getDownloadURL(storageRef);
    }

    if (pdfFile) {
      const storageRef = ref(storage, `books/${Date.now()}_${pdfFile.name}`);
      await uploadBytes(storageRef, pdfFile);
      updateData.pdfUrl = await getDownloadURL(storageRef);
    }

    const docRef = doc(db, COLLECTION, id);
    await updateDoc(docRef, updateData);
  },

  async delete(id: string): Promise<void> {
    const book = await this.getById(id);
    if (book) {
      try {
        if (book.coverImageUrl) {
          const imageRef = ref(storage, book.coverImageUrl);
          await deleteObject(imageRef);
        }
        if (book.pdfUrl) {
          const pdfRef = ref(storage, book.pdfUrl);
          await deleteObject(pdfRef);
        }
      } catch (error) {
        console.error('Error deleting files:', error);
      }
    }
    const docRef = doc(db, COLLECTION, id);
    await deleteDoc(docRef);
  }
};
