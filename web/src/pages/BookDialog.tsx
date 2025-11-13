import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid,
  Avatar,
  Typography,
  MenuItem
} from '@mui/material';
import { bookService } from '../services/bookService';
import { jadidService } from '../services/jadidService';
import { Book, Jadid } from '../types';

interface BookDialogProps {
  open: boolean;
  book: Book | null;
  onClose: () => void;
  onSave: () => void;
}

export default function BookDialog({ open, book, onClose, onSave }: BookDialogProps) {
  const [formData, setFormData] = useState({
    title: '',
    authorId: '',
    authorName: '',
    publishYear: new Date().getFullYear(),
    description: '',
    coverImageUrl: '',
    pdfUrl: '',
    orderIndex: 0
  });
  const [jadids, setJadids] = useState<Jadid[]>([]);
  const [coverFile, setCoverFile] = useState<File | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [coverPreview, setCoverPreview] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) {
      loadJadids();
    }
  }, [open]);

  useEffect(() => {
    if (book) {
      setFormData(book);
      setCoverPreview(book.coverImageUrl);
    } else {
      setFormData({
        title: '',
        authorId: '',
        authorName: '',
        publishYear: new Date().getFullYear(),
        description: '',
        coverImageUrl: '',
        pdfUrl: '',
        orderIndex: 0
      });
      setCoverPreview('');
    }
    setCoverFile(null);
    setPdfFile(null);
  }, [book, open]);

  const loadJadids = async () => {
    console.log('[BookDialog] loadJadids() - Начало загрузки джадидов');
    try {
      const data = await jadidService.getAll();
      console.log(`[BookDialog] loadJadids() - Успешно загружено ${data.length} джадидов`);
      setJadids(data);
    } catch (err) {
      console.error('[BookDialog] loadJadids() - ОШИБКА при загрузке джадидов:', err);
      console.error('[BookDialog] loadJadids() - Детали ошибки:', {
        message: err instanceof Error ? err.message : String(err),
        stack: err instanceof Error ? err.stack : undefined
      });
    }
  };

  const handleCoverChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setCoverFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setCoverPreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handlePdfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setPdfFile(file);
    }
  };

  const handleAuthorChange = (authorId: string) => {
    const author = jadids.find(j => j.id === authorId);
    if (author) {
      setFormData({
        ...formData,
        authorId,
        authorName: author.name
      });
    }
  };

  const handleSubmit = async () => {
    console.log('[BookDialog] handleSubmit() - Начало сохранения', {
      isEdit: !!book,
      bookId: book?.id,
      formData,
      hasCoverFile: !!coverFile,
      coverFileName: coverFile?.name,
      hasPdfFile: !!pdfFile,
      pdfFileName: pdfFile?.name
    });
    try {
      setSaving(true);
      if (book) {
        console.log('[BookDialog] handleSubmit() - Обновление существующей книги');
        await bookService.update(book.id, formData, coverFile || undefined, pdfFile || undefined);
        console.log('[BookDialog] handleSubmit() - Книга успешно обновлена');
      } else {
        console.log('[BookDialog] handleSubmit() - Создание новой книги');
        const newId = await bookService.create(formData, coverFile || undefined, pdfFile || undefined);
        console.log('[BookDialog] handleSubmit() - Книга успешно создана с ID:', newId);
      }
      onSave();
    } catch (err) {
      console.error('[BookDialog] handleSubmit() - ОШИБКА при сохранении:', err);
      console.error('[BookDialog] handleSubmit() - Детали ошибки:', {
        message: err instanceof Error ? err.message : String(err),
        stack: err instanceof Error ? err.stack : undefined,
        name: err instanceof Error ? err.name : undefined,
        formData,
        hasCoverFile: !!coverFile,
        hasPdfFile: !!pdfFile
      });
      alert('Saqlashda xatolik yuz berdi');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{book ? 'Kitobni tahrirlash' : 'Yangi kitob qo\'shish'}</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid item xs={12} display="flex" flexDirection="column" alignItems="center">
            <Avatar variant="rounded" src={coverPreview} sx={{ width: 120, height: 160, mb: 2 }} />
            <Button variant="outlined" component="label" sx={{ mb: 1 }}>
              Muqova tanlash
              <input type="file" hidden accept="image/*" onChange={handleCoverChange} />
            </Button>
            <Button variant="outlined" component="label">
              {pdfFile ? `PDF: ${pdfFile.name}` : 'PDF tanlash'}
              <input type="file" hidden accept=".pdf" onChange={handlePdfChange} />
            </Button>
            {formData.pdfUrl && !pdfFile && (
              <Typography variant="caption" sx={{ mt: 1 }}>
                Joriy PDF: mavjud
              </Typography>
            )}
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Kitob nomi"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Muallif"
              value={formData.authorId}
              onChange={(e) => handleAuthorChange(e.target.value)}
            >
              {jadids.map((jadid) => (
                <MenuItem key={jadid.id} value={jadid.id}>
                  {jadid.name}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12} sm={3}>
            <TextField
              fullWidth
              type="number"
              label="Nashr yili"
              value={formData.publishYear}
              onChange={(e) => setFormData({ ...formData, publishYear: Number(e.target.value) })}
            />
          </Grid>
          <Grid item xs={12} sm={3}>
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
              rows={4}
              label="Tavsif"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
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
