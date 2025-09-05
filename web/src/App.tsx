import { Container, Title } from '@mantine/core';
import MedicalReview from './MedicalReview';

export default function App() {
  return (
    <Container py="md">
      <Title order={2} mb="md">Medical Review</Title>
      <MedicalReview />
    </Container>
  );
}
