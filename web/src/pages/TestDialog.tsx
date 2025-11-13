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
  Typography,
  IconButton,
  Paper,
  Divider,
  RadioGroup,
  Radio,
  FormControlLabel
} from '@mui/material';
import { Add, Delete } from '@mui/icons-material';
import { testService } from '../services/testService';
import { Test, Question } from '../types';

interface TestDialogProps {
  open: boolean;
  test: Test | null;
  onClose: () => void;
  onSave: () => void;
}

export default function TestDialog({ open, test, onClose, onSave }: TestDialogProps) {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    timeLimit: 30,
    questions: [] as Question[]
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (test) {
      setFormData({
        title: test.title,
        description: test.description,
        timeLimit: test.timeLimit,
        questions: test.questions
      });
    } else {
      setFormData({
        title: '',
        description: '',
        timeLimit: 30,
        questions: []
      });
    }
  }, [test, open]);

  const addQuestion = () => {
    setFormData({
      ...formData,
      questions: [
        ...formData.questions,
        {
          id: `q_${Date.now()}`,
          questionText: '',
          answers: ['', '', '', ''],
          correctAnswerIndex: 0
        }
      ]
    });
  };

  const removeQuestion = (index: number) => {
    setFormData({
      ...formData,
      questions: formData.questions.filter((_, i) => i !== index)
    });
  };

  const updateQuestion = (index: number, field: keyof Question, value: any) => {
    const updated = [...formData.questions];
    updated[index] = { ...updated[index], [field]: value };
    setFormData({ ...formData, questions: updated });
  };

  const updateAnswer = (questionIndex: number, answerIndex: number, value: string) => {
    const updated = [...formData.questions];
    const answers = [...updated[questionIndex].answers];
    answers[answerIndex] = value;
    updated[questionIndex] = { ...updated[questionIndex], answers };
    setFormData({ ...formData, questions: updated });
  };

  const handleSubmit = async () => {
    try {
      setSaving(true);
      if (test) {
        await testService.update(test.id, formData);
      } else {
        await testService.create({
          ...formData,
          questionCount: formData.questions.length
        });
      }
      onSave();
    } catch (err) {
      alert('Saqlashda xatolik yuz berdi');
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>{test ? 'Testni tahrirlash' : 'Yangi test qo\'shish'}</DialogTitle>
      <DialogContent>
        <Grid container spacing={2} sx={{ mt: 1 }}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Test nomi"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={8}>
            <TextField
              fullWidth
              label="Tavsif"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              type="number"
              label="Vaqt (daqiqa)"
              value={formData.timeLimit}
              onChange={(e) => setFormData({ ...formData, timeLimit: Number(e.target.value) })}
            />
          </Grid>
          
          <Grid item xs={12}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="h6">Savollar</Typography>
              <Button startIcon={<Add />} onClick={addQuestion}>
                Savol qo'shish
              </Button>
            </Box>
            
            {formData.questions.map((question, qIndex) => (
              <Paper key={question.id} sx={{ p: 2, mb: 2 }}>
                <Box display="flex" justifyContent="space-between" alignItems="start" mb={2}>
                  <Typography variant="subtitle1">{qIndex + 1} - savol</Typography>
                  <IconButton size="small" onClick={() => removeQuestion(qIndex)}>
                    <Delete />
                  </IconButton>
                </Box>
                
                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="Savol matni"
                  value={question.questionText}
                  onChange={(e) => updateQuestion(qIndex, 'questionText', e.target.value)}
                  sx={{ mb: 2 }}
                />
                
                <Typography variant="subtitle2" gutterBottom>
                  Javoblar
                </Typography>
                <RadioGroup
                  value={question.correctAnswerIndex}
                  onChange={(e) => updateQuestion(qIndex, 'correctAnswerIndex', Number(e.target.value))}
                >
                  {question.answers.map((answer, aIndex) => (
                    <Box key={aIndex} display="flex" alignItems="center" mb={1}>
                      <FormControlLabel
                        value={aIndex}
                        control={<Radio />}
                        label={`Javob ${aIndex + 1}`}
                        sx={{ minWidth: 100 }}
                      />
                      <TextField
                        fullWidth
                        size="small"
                        value={answer}
                        onChange={(e) => updateAnswer(qIndex, aIndex, e.target.value)}
                      />
                    </Box>
                  ))}
                </RadioGroup>
              </Paper>
            ))}
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
