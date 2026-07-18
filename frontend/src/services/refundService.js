import api from './api';

export const listRefunds = async () => {
  const response = await api.get('/refunds');
  return response.data.data;
};

export const createRefund = async (refundData) => {
  const response = await api.post('/refunds', refundData);
  return response.data.data;
};
