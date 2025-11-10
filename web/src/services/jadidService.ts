import { collection, doc, getDocs, getDoc, addDoc, updateDoc, deleteDoc, query, orderBy } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL, deleteObject } from 'firebase/storage';
import { db, storage } from './firebase';
import { Jadid } from '../types';

const COLLECTION = 'jadids';

export const jadidService = {
  async getAll(): Promise<Jadid[]> {
    const q = query(collection(db, COLLECTION), orderBy('orderIndex'));
    const snapshot = await getDocs(q);
    return snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Jadid));
  },

  async getById(id: string): Promise<Jadid | null> {
    const docRef = doc(db, COLLECTION, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? { id: docSnap.id, ...docSnap.data() } as Jadid : null;
  },

  async create(data: Omit<Jadid, 'id'>, imageFile?: File): Promise<string> {
    let imageUrl = data.imageUrl;
    
    if (imageFile) {
      const storageRef = ref(storage, `images/jadids/${Date.now()}_${imageFile.name}`);
      await uploadBytes(storageRef, imageFile);
      imageUrl = await getDownloadURL(storageRef);
    }

    const docRef = await addDoc(collection(db, COLLECTION), {
      ...data,
      imageUrl
    });
    return docRef.id;
  },

  async update(id: string, data: Partial<Omit<Jadid, 'id'>>, imageFile?: File): Promise<void> {
    const updateData: Partial<Jadid> = { ...data };
    
    if (imageFile) {
      const storageRef = ref(storage, `images/jadids/${Date.now()}_${imageFile.name}`);
      await uploadBytes(storageRef, imageFile);
      updateData.imageUrl = await getDownloadURL(storageRef);
    }

    const docRef = doc(db, COLLECTION, id);
    await updateDoc(docRef, updateData);
  },

  async delete(id: string): Promise<void> {
    const jadid = await this.getById(id);
    if (jadid?.imageUrl) {
      try {
        const imageRef = ref(storage, jadid.imageUrl);
        await deleteObject(imageRef);
      } catch (error) {
        console.error('Error deleting image:', error);
      }
    }
    const docRef = doc(db, COLLECTION, id);
    await deleteDoc(docRef);
  }
};
