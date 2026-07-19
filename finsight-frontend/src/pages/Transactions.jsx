import { useState, useEffect } from 'react';
import { transactionService } from '../services/api';
import { useNavigate } from 'react-router-dom';

export default function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    title: '', amount: '', type: 'EXPENSE',
    transactionDate: new Date().toISOString().split('T')[0],
    categoryId: 1, merchantName: '', description: ''
  });
  const navigate = useNavigate();

  useEffect(() => { fetchTransactions(); }, []);

  const fetchTransactions = async () => {
    try {
      const res = await transactionService.getAll();
      setTransactions(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await transactionService.create({
        ...form,
        amount: parseFloat(form.amount),
        categoryId: parseInt(form.categoryId)
      });
      setShowForm(false);
      setForm({
        title: '', amount: '', type: 'EXPENSE',
        transactionDate: new Date().toISOString().split('T')[0],
        categoryId: 1, merchantName: '', description: ''
      });
      fetchTransactions();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Delete this transaction?')) {
      await transactionService.delete(id);
      fetchTransactions();
    }
  };

  const categories = [
    { id: 1, name: 'Food' }, { id: 2, name: 'Travel' },
    { id: 3, name: 'Shopping' }, { id: 4, name: 'Bills' },
    { id: 5, name: 'Entertainment' }, { id: 6, name: 'Healthcare' },
    { id: 7, name: 'Education' }, { id: 8, name: 'Other' }
  ];

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <nav className="bg-gray-900 border-b border-gray-800 px-6 py-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-blue-400">FinSight</h1>
        <div className="flex gap-3">
          <button onClick={() => navigate('/dashboard')}
            className="text-sm bg-gray-800 hover:bg-gray-700 px-4 py-2 rounded-lg">
            Dashboard
          </button>
          <button onClick={() => setShowForm(!showForm)}
            className="text-sm bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg">
            + Add Transaction
          </button>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-6 py-8">
        {showForm && (
          <div className="bg-gray-900 rounded-2xl p-6 border border-gray-800 mb-6">
            <h2 className="text-lg font-semibold mb-4">Add Transaction</h2>
            <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
              <input value={form.title}
                onChange={e => setForm({...form, title: e.target.value})}
                placeholder="Title" required
                className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white col-span-2" />
              <input value={form.amount}
                onChange={e => setForm({...form, amount: e.target.value})}
                placeholder="Amount" type="number" step="0.01" required
                className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white" />
              <select value={form.type}
                onChange={e => setForm({...form, type: e.target.value})}
                className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white">
                <option value="EXPENSE">Expense</option>
                <option value="INCOME">Income</option>
              </select>
              <input value={form.transactionDate}
                onChange={e => setForm({...form, transactionDate: e.target.value})}
                type="date" required
                className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white" />
              <select value={form.categoryId}
                onChange={e => setForm({...form, categoryId: e.target.value})}
                className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white">
                {categories.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
              <input value={form.merchantName}
                onChange={e => setForm({...form, merchantName: e.target.value})}
                placeholder="Merchant (optional)"
                className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white col-span-2" />
              <div className="col-span-2 flex gap-3">
                <button type="submit"
                  className="bg-blue-600 hover:bg-blue-700 px-6 py-2 rounded-lg font-medium">
                  Save
                </button>
                <button type="button" onClick={() => setShowForm(false)}
                  className="bg-gray-700 hover:bg-gray-600 px-6 py-2 rounded-lg font-medium">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        <div className="bg-gray-900 rounded-2xl border border-gray-800">
          <div className="p-6 border-b border-gray-800">
            <h2 className="text-lg font-semibold">All Transactions</h2>
          </div>
          {loading ? (
            <p className="text-center text-gray-400 py-8">Loading...</p>
          ) : transactions.length === 0 ? (
            <p className="text-center text-gray-500 py-8">No transactions yet. Add one!</p>
          ) : (
            <div className="divide-y divide-gray-800">
              {transactions.map(t => (
                <div key={t.id} className="flex justify-between items-center px-6 py-4">
                  <div>
                    <p className="font-medium">{t.title}</p>
                    <p className="text-xs text-gray-400">
                      {t.categoryIcon} {t.categoryName} • {t.transactionDate}
                      {t.merchantName && ` • ${t.merchantName}`}
                    </p>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className={`font-bold ${t.type === 'INCOME' ? 'text-green-400' : 'text-red-400'}`}>
                      {t.type === 'INCOME' ? '+' : '-'}₹{t.amount}
                    </span>
                    <button onClick={() => handleDelete(t.id)}
                      className="text-xs text-red-400 hover:text-red-300">
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
