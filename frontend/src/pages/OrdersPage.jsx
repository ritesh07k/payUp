import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { listOrders, createOrder } from '../services/orderService';
import { capturePayment } from '../services/paymentService';

const statusColors = {
  CREATED: 'bg-gray-100 text-gray-700',
  PAID: 'bg-green-100 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
  REFUNDED: 'bg-yellow-100 text-yellow-700',
};

function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('INR');
  const [receipt, setReceipt] = useState('');
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const [capturingOrderId, setCapturingOrderId] = useState(null);
  const [method, setMethod] = useState('card');
  const [captureError, setCaptureError] = useState('');
  const [capturing, setCapturing] = useState(false);

  const fetchOrders = async () => {
    try {
      const data = await listOrders();
      setOrders(data);
    } catch (err) {
      setError('Failed to load orders.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    setFormError('');

    const amountPaise = Math.round(parseFloat(amount) * 100);
    if (!amount || amountPaise <= 0) {
      setFormError('Enter a valid amount greater than 0.');
      return;
    }
    if (!currency.trim()) {
      setFormError('Currency is required.');
      return;
    }

    setSubmitting(true);
    try {
      await createOrder({
        amountPaise,
        currency: currency.trim(),
        receipt: receipt.trim() || undefined,
      });
      setAmount('');
      setCurrency('INR');
      setReceipt('');
      setShowForm(false);
      await fetchOrders();
    } catch (err) {
      setFormError('Failed to create order. Check the values and try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCapture = async (orderId) => {
    setCaptureError('');
    setCapturing(true);
    try {
      await capturePayment({
        orderId,
        method,
        idempotencyKey: crypto.randomUUID(),
      });
      setCapturingOrderId(null);
      setMethod('card');
      await fetchOrders();
    } catch (err) {
      setCaptureError('Failed to capture payment.');
    } finally {
      setCapturing(false);
    }
  };

  return (
    <Layout>
      <div className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-semibold text-gray-800">Orders</h1>
          <button
            onClick={() => setShowForm(!showForm)}
            className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm hover:bg-gray-800"
          >
            {showForm ? 'Cancel' : 'Create Order'}
          </button>
        </div>

        {showForm && (
          <form
            onSubmit={handleCreateOrder}
            className="bg-gray-50 border border-gray-200 rounded-lg p-4 mb-6 space-y-3"
          >
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">
                  Amount ({currency})
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                  placeholder="500.00"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Currency</label>
                <input
                  type="text"
                  value={currency}
                  onChange={(e) => setCurrency(e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                  placeholder="INR"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">
                  Receipt (optional)
                </label>
                <input
                  type="text"
                  value={receipt}
                  onChange={(e) => setReceipt(e.target.value)}
                  className="w-full border border-gray-300 rounded-md px-3 py-2 text-sm"
                  placeholder="order_ref_001"
                />
              </div>
            </div>

            {formError && <p className="text-red-600 text-sm">{formError}</p>}

            <button
              type="submit"
              disabled={submitting}
              className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm hover:bg-gray-800 disabled:opacity-50"
            >
              {submitting ? 'Creating...' : 'Submit'}
            </button>
          </form>
        )}

        {loading && <p className="text-gray-500">Loading orders...</p>}
        {error && <p className="text-red-600">{error}</p>}

        {!loading && !error && (
          <div className="bg-gray-50 rounded-lg overflow-hidden border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-100 text-gray-600 text-left">
                <tr>
                  <th className="px-4 py-3">Order ID</th>
                  <th className="px-4 py-3">Receipt</th>
                  <th className="px-4 py-3">Amount</th>
                  <th className="px-4 py-3">Currency</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Action</th>
                </tr>
              </thead>
              <tbody>
                {orders.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="px-4 py-6 text-center text-gray-400">
                      No orders yet.
                    </td>
                  </tr>
                ) : (
                  orders.map((order) => (
                    <tr key={order.id} className="border-t border-gray-200">
                      <td className="px-4 py-3 font-mono text-xs text-gray-600">{order.id}</td>
                      <td className="px-4 py-3">{order.receipt}</td>
                      <td className="px-4 py-3">{(order.amountPaise / 100).toFixed(2)}</td>
                      <td className="px-4 py-3">{order.currency}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusColors[order.status] || 'bg-gray-100 text-gray-700'}`}>
                          {order.status}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        {order.status === 'CREATED' && (
                          capturingOrderId === order.id ? (
                            <div className="flex items-center gap-2">
                              <select
                                value={method}
                                onChange={(e) => setMethod(e.target.value)}
                                className="border border-gray-300 rounded-md px-2 py-1 text-xs"
                              >
                                <option value="card">Card</option>
                                <option value="upi">UPI</option>
                                <option value="netbanking">Netbanking</option>
                              </select>
                              <button
                                onClick={() => handleCapture(order.id)}
                                disabled={capturing}
                                className="bg-gray-900 text-white px-2 py-1 rounded-md text-xs hover:bg-gray-800 disabled:opacity-50"
                              >
                                {capturing ? '...' : 'Confirm'}
                              </button>
                              <button
                                onClick={() => setCapturingOrderId(null)}
                                className="text-gray-500 text-xs hover:underline"
                              >
                                Cancel
                              </button>
                            </div>
                          ) : (
                            <button
                              onClick={() => { setCapturingOrderId(order.id); setCaptureError(''); }}
                              className="text-blue-600 text-xs font-medium hover:underline"
                            >
                              Capture
                            </button>
                          )
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
        {captureError && <p className="text-red-600 text-sm mt-2">{captureError}</p>}
      </div>
    </Layout>
  );
}

export default OrdersPage;
