import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import API from '../services/api';

export default function Chat() {
  const navigate = useNavigate();
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      text: 'Hi! I am your FinSight AI assistant. Ask me anything about your finances!'
    }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const suggestions = [
    'Where did I spend the most money?',
    'How much did I spend on food?',
    'What is my savings rate?',
    'Am I spending too much on entertainment?',
  ];

  const sendMessage = async (text) => {
    const userMessage = text || input;
    if (!userMessage.trim()) return;

    setMessages(prev => [...prev, { role: 'user', text: userMessage }]);
    setInput('');
    setLoading(true);

    try {
      const res = await API.post('/ai/chat', { message: userMessage });
      setMessages(prev => [...prev, {
        role: 'assistant',
        text: res.data.insight
      }]);
    } catch (err) {
      setMessages(prev => [...prev, {
        role: 'assistant',
        text: 'Sorry, I could not process your question. Please try again.'
      }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-950 text-white flex flex-col">
      <nav className="bg-gray-900 border-b border-gray-800 px-6 py-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-blue-400">FinSight AI</h1>
        <div className="flex gap-3">
          <button onClick={() => navigate('/dashboard')}
            className="text-sm bg-gray-800 hover:bg-gray-700 px-4 py-2 rounded-lg">
            Dashboard
          </button>
          <button onClick={() => navigate('/transactions')}
            className="text-sm bg-gray-800 hover:bg-gray-700 px-4 py-2 rounded-lg">
            Transactions
          </button>
        </div>
      </nav>

      <div className="flex-1 max-w-3xl w-full mx-auto px-6 py-6 flex flex-col">
        <div className="mb-4">
          <h2 className="text-lg font-semibold mb-2 text-gray-300">Quick Questions</h2>
          <div className="flex flex-wrap gap-2">
            {suggestions.map((s, i) => (
              <button key={i} onClick={() => sendMessage(s)}
                className="text-xs bg-gray-800 hover:bg-blue-600 border border-gray-700 px-3 py-2 rounded-full transition">
                {s}
              </button>
            ))}
          </div>
        </div>

        <div className="flex-1 bg-gray-900 rounded-2xl border border-gray-800 p-4 overflow-y-auto mb-4 space-y-4 min-h-96">
          {messages.map((msg, i) => (
            <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-xs md:max-w-md lg:max-w-lg px-4 py-3 rounded-2xl text-sm whitespace-pre-wrap ${
                msg.role === 'user'
                  ? 'bg-blue-600 text-white rounded-br-none'
                  : 'bg-gray-800 text-gray-100 rounded-bl-none'
              }`}>
                {msg.text}
              </div>
            </div>
          ))}
          {loading && (
            <div className="flex justify-start">
              <div className="bg-gray-800 px-4 py-3 rounded-2xl rounded-bl-none">
                <div className="flex gap-1">
                  <div className="w-2 h-2 bg-blue-400 rounded-full animate-bounce"></div>
                  <div className="w-2 h-2 bg-blue-400 rounded-full animate-bounce delay-100"></div>
                  <div className="w-2 h-2 bg-blue-400 rounded-full animate-bounce delay-200"></div>
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="flex gap-3">
          <input
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && sendMessage()}
            placeholder="Ask about your finances..."
            className="flex-1 bg-gray-900 border border-gray-700 text-white rounded-xl px-4 py-3 focus:outline-none focus:border-blue-500"
          />
          <button
            onClick={() => sendMessage()}
            disabled={loading || !input.trim()}
            className="bg-blue-600 hover:bg-blue-700 disabled:opacity-50 px-6 py-3 rounded-xl font-medium transition">
            Send
          </button>
        </div>
      </div>
    </div>
  );
}
