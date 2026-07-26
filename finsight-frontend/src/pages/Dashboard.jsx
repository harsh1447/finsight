import { useState, useEffect } from 'react';
import { transactionService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';

const COLORS = ['#3B82F6', '#14B8A6', '#8B5CF6', '#F59E0B', '#22C55E', '#EF4444', '#EC4899', '#94A3B8'];

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [summaryRes, transRes] = await Promise.all([
        transactionService.getSummary(),
        transactionService.getAll(),
      ]);
      setSummary(summaryRes.data);
      setTransactions(transRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const categoryData = transactions
    .filter(t => t.type === 'EXPENSE')
    .reduce((acc, t) => {
      const cat = t.categoryName || 'Other';
      acc[cat] = (acc[cat] || 0) + Number(t.amount);
      return acc;
    }, {});

  const pieData = Object.entries(categoryData).map(([name, value]) => ({ name, value }));

  if (loading) return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <p className="text-white text-xl">Loading...</p>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <nav className="bg-gray-900 border-b border-gray-800 px-6 py-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-blue-400">FinSight</h1>
        <div className="flex items-center gap-4">
          <span className="text-gray-400 text-sm">Hi, {user?.fullName}</span>
          <button onClick={() => navigate('/transactions')}
            className="text-sm bg-gray-800 hover:bg-gray-700 px-4 py-2 rounded-lg transition">
            Transactions
          </button>
          <button onClick={() => navigate('/chat')}
            className="text-sm bg-purple-600 hover:bg-purple-700 px-4 py-2 rounded-lg transition">
            AI Chat
          </button>
          <button onClick={handleLogout}
            className="text-sm bg-red-600 hover:bg-red-700 px-4 py-2 rounded-lg transition">
            Logout
          </button>
        </div>
      </nav>

      <div className="max-w-6xl mx-auto px-6 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-gray-900 rounded-2xl p-6 border border-gray-800">
            <p className="text-gray-400 text-sm mb-1">Total Income</p>
            <p className="text-3xl font-bold text-green-400">₹{summary?.totalIncome || 0}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-6 border border-gray-800">
            <p className="text-gray-400 text-sm mb-1">Total Expenses</p>
            <p className="text-3xl font-bold text-red-400">₹{summary?.totalExpenses || 0}</p>
          </div>
          <div className="bg-gray-900 rounded-2xl p-6 border border-gray-800">
            <p className="text-gray-400 text-sm mb-1">Savings</p>
            <p className={`text-3xl font-bold ${summary?.savings >= 0 ? 'text-blue-400' : 'text-red-400'}`}>
              ₹{summary?.savings || 0}
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-gray-900 rounded-2xl p-6 border border-gray-800">
            <h2 className="text-lg font-semibold mb-4">Expenses by Category</h2>
            {pieData.length > 0 ? (
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" outerRadius={80} dataKey="value"
                    label={({ name }) => name}>
                    {pieData.map((_, index) => (
                      <Cell key={index} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => `₹${value}`} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-64 flex items-center justify-center text-gray-500">
                No expense data yet
              </div>
            )}
          </div>

          <div className="bg-gray-900 rounded-2xl p-6 border border-gray-800">
            <h2 className="text-lg font-semibold mb-4">Recent Transactions</h2>
            <div className="space-y-3 overflow-y-auto max-h-64">
              {transactions.slice(0, 5).map(t => (
                <div key={t.id} className="flex justify-between items-center py-2 border-b border-gray-800">
                  <div>
                    <p className="text-sm font-medium">{t.title}</p>
                    <p className="text-xs text-gray-400">{t.categoryName} • {t.transactionDate}</p>
                  </div>
                  <span className={`font-semibold ${t.type === 'INCOME' ? 'text-green-400' : 'text-red-400'}`}>
                    {t.type === 'INCOME' ? '+' : '-'}₹{t.amount}
                  </span>
                </div>
              ))}
              {transactions.length === 0 && (
                <p className="text-gray-500 text-sm text-center py-8">No transactions yet</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
