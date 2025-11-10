import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Typography,
  Avatar
} from '@mui/material';
import { Add, Edit, Delete } from '@mui/icons-material';
import { jadidService } from '../services/jadidService';
import { Jadid } from '../types';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import ConfirmDialog from '../components/ConfirmDialog';
import JadidDialog from './JadidDialog';

export default function JadidsPage() {
  const [jadids, setJadids] = useState<Jadid[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingJadid, setEditingJadid] = useState<Jadid | null>(null);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; id: string }>({ open: false, id: '' });

  const loadJadids = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await jadidService.getAll();
      setJadids(data);
    } catch (err) {
      setError('Ma\'lumotlarni yuklashda xatolik yuz berdi');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadJadids();
  }, []);

  const handleAdd = () => {
    setEditingJadid(null);
    setDialogOpen(true);
  };

  const handleEdit = (jadid: Jadid) => {
    setEditingJadid(jadid);
    setDialogOpen(true);
  };

  const handleDelete = async () => {
    try {
      await jadidService.delete(deleteDialog.id);
      setDeleteDialog({ open: false, id: '' });
      loadJadids();
    } catch (err) {
      setError('O\'chirishda xatolik yuz berdi');
    }
  };

  const handleSave = () => {
    setDialogOpen(false);
    loadJadids();
  };

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage message={error} onRetry={loadJadids} />;

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Jadidlar</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={handleAdd}>
          Qo'shish
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Rasm</TableCell>
              <TableCell>Ism</TableCell>
              <TableCell>Ism (Uz)</TableCell>
              <TableCell>Tug'ilgan yil</TableCell>
              <TableCell>Vafot etgan yil</TableCell>
              <TableCell>Tartib</TableCell>
              <TableCell align="right">Amallar</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {jadids.map((jadid) => (
              <TableRow key={jadid.id}>
                <TableCell>
                  <Avatar src={jadid.imageUrl} alt={jadid.name} />
                </TableCell>
                <TableCell>{jadid.name}</TableCell>
                <TableCell>{jadid.nameUz}</TableCell>
                <TableCell>{jadid.birthYear}</TableCell>
                <TableCell>{jadid.deathYear}</TableCell>
                <TableCell>{jadid.orderIndex}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => handleEdit(jadid)}>
                    <Edit />
                  </IconButton>
                  <IconButton onClick={() => setDeleteDialog({ open: true, id: jadid.id })}>
                    <Delete />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <JadidDialog
        open={dialogOpen}
        jadid={editingJadid}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />

      <ConfirmDialog
        open={deleteDialog.open}
        title="Jadidni o'chirish"
        message="Ushbu jadidni o'chirishni xohlaysizmi? Bu amalni qaytarib bo'lmaydi."
        onConfirm={handleDelete}
        onCancel={() => setDeleteDialog({ open: false, id: '' })}
      />
    </Box>
  );
}
