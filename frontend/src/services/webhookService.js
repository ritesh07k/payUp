import api from './api';

export const listWebhooks = async () => {
  const response = await api.get('/webhooks');
  return response.data.data;
};

export const registerWebhook = async (url) => {
  const response = await api.post('/webhooks', { url });
  return response.data.data;
};
