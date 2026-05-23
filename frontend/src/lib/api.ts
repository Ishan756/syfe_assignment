import type {
  AuthResponse,
  CategoriesEnvelope,
  CategoryResponse,
  GoalResponse,
  GoalsEnvelope,
  LoginRequest,
  MessageResponse,
  MonthlyReportResponse,
  RegisterRequest,
  TransactionResponse,
  TransactionsEnvelope,
  YearlyReportResponse,
} from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL?.trim() || '/api';

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers || {}),
    },
    ...init,
  });

  const text = await response.text();
  const payload = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new ApiError(response.status, payload?.message || 'Request failed');
  }

  return payload as T;
}

function queryString(params: Record<string, string | number | undefined | null>) {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, String(value));
    }
  });

  const output = search.toString();
  return output ? `?${output}` : '';
}

export const api = {
  register(payload: RegisterRequest) {
    return request<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  login(payload: LoginRequest) {
    return request<MessageResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  logout() {
    return request<MessageResponse>('/auth/logout', {
      method: 'POST',
    });
  },
  getCategories() {
    return request<CategoriesEnvelope>('/categories');
  },
  createCategory(payload: { name: string; type: 'INCOME' | 'EXPENSE' }) {
    return request<CategoryResponse>('/categories', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  deleteCategory(name: string) {
    return request<MessageResponse>(`/categories/${encodeURIComponent(name)}`, {
      method: 'DELETE',
    });
  },
  getTransactions(filters?: {
    startDate?: string;
    endDate?: string;
    categoryId?: number | '';
  }) {
    return request<TransactionsEnvelope>(
      `/transactions${queryString(filters || {})}`,
    );
  },
  createTransaction(payload: {
    amount: number;
    date: string;
    category: string;
    description?: string;
  }) {
    return request<TransactionResponse>('/transactions', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  updateTransaction(
    id: number,
    payload: {
      amount?: number;
      category?: string;
      description?: string;
    },
  ) {
    return request<TransactionResponse>(`/transactions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
  deleteTransaction(id: number) {
    return request<MessageResponse>(`/transactions/${id}`, {
      method: 'DELETE',
    });
  },
  getGoals() {
    return request<GoalsEnvelope>('/goals');
  },
  createGoal(payload: {
    goalName: string;
    targetAmount: number;
    targetDate: string;
    startDate?: string;
  }) {
    return request<GoalResponse>('/goals', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
  updateGoal(id: number, payload: { targetAmount?: number; targetDate?: string }) {
    return request<GoalResponse>(`/goals/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  },
  deleteGoal(id: number) {
    return request<MessageResponse>(`/goals/${id}`, {
      method: 'DELETE',
    });
  },
  getMonthlyReport(year: number, month: number) {
    return request<MonthlyReportResponse>(`/reports/monthly/${year}/${month}`);
  },
  getYearlyReport(year: number) {
    return request<YearlyReportResponse>(`/reports/yearly/${year}`);
  },
};
