import { useState } from 'react';
import { TextInput, Button, Stack, Code } from '@mantine/core';
import { getMedicalReviewSummary, getMedicalReviewHistory } from './api';

export default function MedicalReview() {
  const [patientId, setPatientId] = useState('');
  const [summary, setSummary] = useState<any>(null);
  const [history, setHistory] = useState<any>(null);

  const loadData = async () => {
    if (!patientId) return;
    try {
      const summaryData = await getMedicalReviewSummary(patientId);
      setSummary(summaryData);
      const historyData = await getMedicalReviewHistory(patientId);
      setHistory(historyData);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Stack>
      <TextInput label="Patient ID" value={patientId} onChange={(e) => setPatientId(e.currentTarget.value)} />
      <Button onClick={loadData}>Load</Button>
      {summary && (
        <div>
          <h3>Summary</h3>
          <Code block>{JSON.stringify(summary, null, 2)}</Code>
        </div>
      )}
      {history && (
        <div>
          <h3>History</h3>
          <Code block>{JSON.stringify(history, null, 2)}</Code>
        </div>
      )}
    </Stack>
  );
}
