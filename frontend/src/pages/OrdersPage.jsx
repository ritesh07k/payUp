import { useState, useEffect } from 'react';
import Layout from '../components/Layout';
import { listOrders } from '../services/orderService';

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

  useEffect(() => {
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
    fetchOrders();
  }, []);

  return (
    <Layout>
      <div className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-semibold text-gray-800">Orders</h1>
          <button className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm hover:bg-gray-800">
            Create Order
          </button>
        </div>

        {loading && <p className="text-gray-500">Loading orders...</p>}
        {error && <p className="text-red-600">{error}</p>}

        {!loading && !error && (
          <div className="bg-gray-50 rounded-lg overflow-hidden border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-100 text-gray-600 text-left">
                <tr>
                  <th className="px-4 py-3">Order ID</th>
                  <th className="px-4 py-3">Amount</th>
                  <th className="px-4 py-3">Currency</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Created At</th>
                </tr>
              </thead>
              <tbody>
                {orders.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-4 py-6 text-center text-gray-400">
                      No orders yet.
                    </td>
                  </tr>
                ) : (
                  orders.map((order) => (
                    <tr key={order.id} className="border-t border-gray-200">
                      <td className="px-4 py-3 font-mono text-xs text-gray-600">{order.id}</td>
                      <td className="px-4 py-3">{order.amount}</td>
                      <td className="px-4 py-3">{order.currency}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusColors[order.status] || 'bg-gray-100 text-gray-700'}`}>
                          {order.status}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-gray-500">
                        {new Date(order.createdAt).toLocaleString()}
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

export default OrdersPage;