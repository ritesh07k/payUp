import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { listWebhooks, registerWebhook } from '../services/webhookService';

function WebhooksPage() {
  const [webhooks, setWebhooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [url, setUrl] = useState('');
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [newSecret, setNewSecret] = useState('');

  const fetchWebhooks = async () => {
    try {
      const data = await listWebhooks();
      setWebhooks(data);
    } catch (err) {
      setError('Failed to load webhooks.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWebhooks();
  }, []);

  const handleRegister = async (e) => {
    e.preventDefault();
    setFormError('');
    setNewSecret('');

    if (!url.trim()) {
      setFormError('URL is required.');
      return;
    }

    setSubmitting(true);
    try {
      const result = await registerWebhook(url.trim());
      setNewSecret(result.secret);
      setUrl('');
      await fetchWebhooks();
    } catch (err) {
      setFormError('Failed to register webhook. Check the URL and try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout>
      <div className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-semibold text-gray-800">Webhooks</h1>
          <button
            onClick={() => { setShowForm(!showForm); setNewSecret(''); setFormError(''); }}
            className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm hover:bg-gray-800"
          >
            {showForm ? 'Cancel' : 'Register Endpoint'}
          </button>
        </div>

        {showForm && (
          <form
            onSubmit={handleRegister}
            className="bg-gray-50 border border-gray-200 rounded-lg p-4 mb-6 space-y-3"
          >
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Endpoint URL</label>
              <input
                type="url"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                placeholder="https://your-server.com/webhooks/payup"
              />
            </div>

            {formError && <p className="text-red-600 text-sm">{formError}</p>}

            {newSecret && (
              <div className="bg-yellow-50 border border-yellow-200 rounded-md p-3 text-sm">
                <p className="text-yellow-800 font-medium mb-1">
                  Save this secret now — it won't be shown again:
                </p>
                <code className="text-xs break-all text-yellow-900">{newSecret}</code>
              </div>
            )}

            <button
              type="submit"
              disabled={submitting}
              className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm hover:bg-gray-800 disabled:opacity-50"
            >
              {submitting ? 'Registering...' : 'Register'}
            </button>
          </form>
        )}

        {loading && <p className="text-gray-500">Loading webhooks...</p>}
        {error && <p className="text-red-600">{error}</p>}

        {!loading && !error && (
          <div className="bg-gray-50 rounded-lg overflow-hidden border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-100 text-gray-600 text-left">
                <tr>
                  <th className="px-4 py-3">Endpoint ID</th>
                  <th className="px-4 py-3">URL</th>
                  <th className="px-4 py-3">Status</th>
                </tr>
              </thead>
              <tbody>
                {webhooks.length === 0 ? (
                  <tr>
                    <td colSpan="3" className="px-4 py-6 text-center text-gray-400">
                      No webhook endpoints registered yet.
                    </td>
                  </tr>
                ) : (
                  webhooks.map((wh) => (
                    <tr key={wh.id} className="border-t border-gray-200">
                      <td className="px-4 py-3 font-mono text-xs text-gray-600">{wh.id}</td>
                      <td className="px-4 py-3">{wh.url}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${wh.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'}`}>
                          {wh.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  );
}

export default WebhooksPage;
