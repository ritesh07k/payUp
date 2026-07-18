import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { listRefunds } from '../services/refundService';

const statusColors = {
  PROCESSED: 'bg-green-100 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
  PENDING: 'bg-gray-100 text-gray-700',
};

function RefundsPage() {
  const [refunds, setRefunds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchRefunds = async () => {
      try {
        const data = await listRefunds();
        setRefunds(data);
      } catch (err) {
        setError('Failed to load refunds.');
      } finally {
        setLoading(false);
      }
    };
    fetchRefunds();
  }, []);

  return (
    <Layout>
      <div className="p-6">
        <h1 className="text-2xl font-semibold text-gray-800 mb-6">Refunds</h1>

        {loading && <p className="text-gray-500">Loading refunds...</p>}
        {error && <p className="text-red-600">{error}</p>}

        {!loading && !error && (
          <div className="bg-gray-50 rounded-lg overflow-hidden border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-100 text-gray-600 text-left">
                <tr>
                  <th className="px-4 py-3">Refund ID</th>
                  <th className="px-4 py-3">Payment ID</th>
                  <th className="px-4 py-3">Amount</th>
                  <th className="px-4 py-3">Currency</th>
                  <th className="px-4 py-3">Status</th>
                </tr>
              </thead>
              <tbody>
                {refunds.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-4 py-6 text-center text-gray-400">
                      No refunds yet.
                    </td>
                  </tr>
                ) : (
                  refunds.map((refund) => (
                    <tr key={refund.id} className="border-t border-gray-200">
                      <td className="px-4 py-3 font-mono text-xs text-gray-600">{refund.id}</td>
                      <td className="px-4 py-3 font-mono text-xs text-gray-600">{refund.paymentId}</td>
                      <td className="px-4 py-3">{(refund.amountPaise / 100).toFixed(2)}</td>
                      <td className="px-4 py-3">{refund.currency}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusColors[refund.status] || 'bg-gray-100 text-gray-700'}`}>
                          {refund.status}
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

export default RefundsPage;
