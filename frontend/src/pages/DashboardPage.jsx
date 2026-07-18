import { useState, useEffect } from 'react'
import Layout from '../components/Layout'
import { getMerchantProfile } from '../services/merchantService'
import { listPayments } from '../services/paymentService'
import { listRefunds } from '../services/refundService'
import { listWebhooks } from '../services/webhookService'

function DashboardPage() {
  const [merchant, setMerchant] = useState(null)
  const [loading, setLoading] = useState(true)
  const [totalVolume, setTotalVolume] = useState(null)
  const [successRate, setSuccessRate] = useState(null)
  const [activeWebhooks, setActiveWebhooks] = useState(null)

  useEffect(() => {
    getMerchantProfile()
      .then((result) => setMerchant(result.data))
      .catch((err) => console.error('Failed to load merchant profile', err))
      .finally(() => setLoading(false))

    Promise.all([listPayments(), listRefunds(), listWebhooks()])
      .then(([payments, refunds, webhooks]) => {
        const captured = payments.filter((p) => p.status === 'CAPTURED')
        const failed = payments.filter((p) => p.status === 'FAILED')

        const capturedTotal = captured.reduce((sum, p) => sum + p.amountPaise, 0)
        const refundedTotal = refunds.reduce((sum, r) => sum + r.amountPaise, 0)
        setTotalVolume((capturedTotal - refundedTotal) / 100)

        const totalAttempts = captured.length + failed.length
        setSuccessRate(totalAttempts > 0 ? (captured.length / totalAttempts) * 100 : null)

        setActiveWebhooks(webhooks.filter((w) => w.active).length)
      })
      .catch((err) => console.error('Failed to load dashboard metrics', err))
  }, [])

  return (
    <Layout>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-medium text-gray-900">Dashboard</h1>
        {!loading && merchant && (
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <div className="w-7 h-7 rounded-full bg-blue-50 text-blue-700 flex items-center justify-center text-xs font-medium">
              {merchant.businessName?.slice(0, 2).toUpperCase()}
            </div>
            {merchant.businessName}
          </div>
        )}
      </div>
      <div className="grid grid-cols-3 gap-3">
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="text-sm text-gray-500 mb-1">Total volume</div>
          <div className="text-2xl font-medium">
            {totalVolume === null ? '—' : `₹${totalVolume.toFixed(2)}`}
          </div>
        </div>
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="text-sm text-gray-500 mb-1">Success rate</div>
          <div className="text-2xl font-medium">
            {successRate === null ? '—' : `${successRate.toFixed(0)}%`}
          </div>
        </div>
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="text-sm text-gray-500 mb-1">Active webhooks</div>
          <div className="text-2xl font-medium">
            {activeWebhooks === null ? '—' : activeWebhooks}
          </div>
        </div>
      </div>
    </Layout>
  )
}
export default DashboardPage
