const API_BASE = import.meta.env.VITE_API_BASE_URL || '';

export async function getMedicalReviewSummary(patientId: string) {
  const res = await fetch(`${API_BASE}/spice-service/medical-review/summary`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ patientId })
  });
  if (!res.ok) {
    throw new Error('Failed to fetch summary');
  }
  return res.json();
}

export async function getMedicalReviewHistory(patientId: string) {
  const res = await fetch(`${API_BASE}/spice-service/medical-review/list`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ patientId })
  });
  if (!res.ok) {
    throw new Error('Failed to fetch history');
  }
  return res.json();
}
