import api from './api';

export const listPayments = async () => {
  const response = await api.get('/payments');
  return response.data.data;
};

export const capturePayment = async (paymentData) => {
  const response = await api.post('/payments/capture', paymentData);
  return response.data.data;
};
