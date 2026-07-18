import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { listPayments } from '../services/paymentService';
import { listRefunds, createRefund } from '../services/refundService';

const statusColors = {
  CAPTURED: 'bg-green-100 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
  REFUNDED: 'bg-yellow-100 text-yellow-700',
};

function PaymentsPage() {
  const [payments, setPayments] = useState([]);
  const [refundedPaymentIds, setRefundedPaymentIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [refundingPaymentId, setRefundingPaymentId] = useState(null);
  const [refundAmount, setRefundAmount] = useState('');
  const [refundReason, setRefundReason] = useState('');
  const [refundError, setRefundError] = useState('');
  const [refunding, setRefunding] = useState(false);

  const fetchData = async () => {
    try {
      const [paymentsData, refundsData] = await Promise.all([listPayments(), listRefunds()]);
      setPayments(paymentsData);
      setRefundedPaymentIds(new Set(refundsData.map((r) => r.paymentId)));
    } catch (err) {
      setError('Failed to load payments.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const openRefundForm = (payment) => {
    setRefundingPaymentId(payment.id);
    setRefundAmount((payment.amountPaise / 100).toFixed(2));
    setRefundReason('');
    setRefundError('');
  };

  const handleRefund = async (paymentId) => {
    setRefundError('');
    const amountPaise = Math.round(parseFloat(refundAmount) * 100);
    if (!refundAmount || amountPaise <= 0) {
      setRefundError('Enter a valid amount greater than 0.');
      return;
    }

    setRefunding(true);
    try {
      await createRefund({
        paymentId,
        amountPaise,
        reason: refundReason.trim() || undefined,
        idempotencyKey: crypto.randomUUID(),
      });
      setRefundingPaymentId(null);
      await fetchData();
    } catch (err) {
      setRefundError('Failed to process refund.');
    } finally {
      setRefunding(false);
    }
  };

  return (
    <Layout>
      <div className="p-6">
        <h1 className="text-2xl font-semibold text-gray-800 mb-6">Payments</h1>

        {loading && <p className="text-gray-500">Loading payments...</p>}
        {error && <p className="text-red-600">{error}</p>}

        {!loading && !error && (
          <div className="bg-gray-50 rounded-lg overflow-hidden border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-100 text-gray-600 text-left">
                <tr>
                  <th className="px-4 py-3">Payment ID</th>
                  <th className="px-4 py-3">Order ID</th>
                  <th className="px-4 py-3">Amount</th>
                  <th className="px-4 py-3">Currency</th>
                  <th className="px-4 py-3">Method</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Action</th>
                </tr>
              </thead>
              <tbody>
                {payments.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="px-4 py-6 text-center text-gray-400">
                      No payments yet.
                    </td>
                  </tr>
                ) : (
                  payments.map((payment) => {
                    const alreadyRefunded = refundedPaymentIds.has(payment.id);
                    return (
                      <tr key={payment.id} className="border-t border-gray-200">
                        <td className="px-4 py-3 font-mono text-xs text-gray-600">{payment.id}</td>
                        <td className="px-4 py-3 font-mono text-xs text-gray-600">{payment.orderId}</td>
                        <td className="px-4 py-3">{(payment.amountPaise / 100).toFixed(2)}</td>
                        <td className="px-4 py-3">{payment.currency}</td>
                        <td className="px-4 py-3">{payment.method}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusColors[alreadyRefunded ? 'REFUNDED' : payment.status] || 'bg-gray-100 text-gray-700'}`}>
                            {alreadyRefunded ? 'REFUNDED' : payment.status}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          {payment.status === 'CAPTURED' && !alreadyRefunded && (
                            refundingPaymentId === payment.id ? (
                              <div className="flex items-center gap-2 flex-wrap">
                                <input
                                  type="number"
                                  step="0.01"
                                  min="0.01"
                                  value={refundAmount}
                                  onChange={(e) => setRefundAmount(e.target.value)}
                                  className="border border-gray-300 rounded-md px-2 py-1 text-xs w-20"
                                />
                                <input
                                  type="text"
                                  value={refundReason}
                                  onChange={(e) => setRefundReason(e.target.value)}
                                  placeholder="Reason (optional)"
                                  className="border border-gray-300 rounded-md px-2 py-1 text-xs w-32"
                                />
                                <button
                                  onClick={() => handleRefund(payment.id)}
                                  disabled={refunding}
                                  className="bg-gray-900 text-white px-2 py-1 rounded-md text-xs hover:bg-gray-800 disabled:opacity-50"
                                >
                                  {refunding ? '...' : 'Confirm'}
                                </button>
                                <button
                                  onClick={() => setRefundingPaymentId(null)}
                                  className="text-gray-500 text-xs hover:underline"
                                >
                                  Cancel
                                </button>
                              </div>
                            ) : (
                              <button
                                onClick={() => openRefundForm(payment)}
                                className="text-blue-600 text-xs font-medium hover:underline"
                              >
                                Refund
                              </button>
                            )
                          )}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        )}
        {refundError && <p className="text-red-600 text-sm mt-2">{refundError}</p>}
      </div>
    </Layout>
  );
}

export default PaymentsPage;
