import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid,
  Box,
  Avatar
} from '@mui/material';
import { jadidService } from '../services/jadidService';
import { Jadid } from '../types';

interface JadidDialogProps {
  open: boolean;
  jadid: Jadid | null;
  onClose: () => void;
  onSave: () => void;
}

export default function JadidDialog({ open, jadid, onClose, onSave }: JadidDialogProps) {
  const [formData, setFormData] = useState({
    name: '',
    nameUz: '',
    birthYear: new Date().getFullYear(),
    deathYear: new Date().getFullYear(),
    shortDescription: '',
    fullDescription: '',
    imageUrl: '',
    orderIndex: 0
  });
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (jadid) {
      setFormData(jadid);
      setImagePreview(jadid.imageUrl);
    } else {
      setFormData({
        name: '',
        nameUz: '',
        birthYear: new Date().getFullYear(),
        deathYear: new Date().getFullYear(),
        shortDescription: '',
        fullDescription: '',
        imageUrl: '',
        orderIndex: 0
      });
      setImagePreview('');
    }
    setImageFile(null);
  }, [jadid, open]);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setImageFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async () => {
    try {
      setSaving(true);
      if (jadid) {
        await jadidService.update(jadid.id, formData, imageFile || undefined);
      } else {
        await jadidService.create(formData, imageFile || undefined);
      }
      onSave();
    } catch (err) {
      alert('Saqlashda xatolik yuz berdi');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{jadid ? 'Jadidni tahrirlash' : 'Yangi jadid qo\'shish'}</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid item xs={12} display="flex" flexDirection="column" alignItems="center">
            <Avatar src={imagePreview} sx={{ width: 120, height: 120, mb: 2 }} />
            <Button variant="outlined" component="label">
              Rasm tanlash
              <input type="file" hidden accept="image/*" onChange={handleImageChange} />
            </Button>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Ism"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Ism (O'zbekcha)"
              value={formData.nameUz}
              onChange={(e) => setFormData({ ...formData, nameUz: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              type="number"
              label="Tug'ilgan yil"
              value={formData.birthYear}
              onChange={(e) => setFormData({ ...formData, birthYear: Number(e.target.value) })}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              type="number"
              label="Vafot etgan yil"
              value={formData.deathYear}
              onChange={(e) => setFormData({ ...formData, deathYear: Number(e.target.value) })}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              type="number"
              label="Tartib raqami"
              value={formData.orderIndex}
              onChange={(e) => setFormData({ ...formData, orderIndex: Number(e.target.value) })}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              multiline
              rows={2}
              label="Qisqacha tavsif"
              value={formData.shortDescription}
              onChange={(e) => setFormData({ ...formData, shortDescription: e.target.value })}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              multiline
              rows={4}
              label="To'liq tavsif"
              value={formData.fullDescription}
              onChange={(e) => setFormData({ ...formData, fullDescription: e.target.value })}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Bekor qilish</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={saving}>
          {saving ? 'Saqlanmoqda...' : 'Saqlash'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
