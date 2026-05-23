export type CategoryType = 'INCOME' | 'EXPENSE';

export interface AuthResponse {
  message: string;
  userId?: number;
}

export interface MessageResponse {
  message: string;
}

export interface CategoryResponse {
  id: number;
  name: string;
  type: CategoryType;
  isCustom: boolean;
}

export interface TransactionResponse {
  id: number;
  amount: number;
  date: string;
  category: string;
  description?: string | null;
  type: CategoryType;
}

export interface GoalResponse {
  id: number;
  goalName: string;
  targetAmount: number;
  targetDate: string;
  startDate: string;
  currentProgress: number;
  progressPercentage: number;
  remainingAmount: number;
}

export interface MonthlyReportResponse {
  month: number;
  year: number;
  totalIncome: Record<string, number>;
  totalExpenses: Record<string, number>;
  netSavings: number;
}

export interface YearlyReportResponse {
  year: number;
  totalIncome: Record<string, number>;
  totalExpenses: Record<string, number>;
  netSavings: number;
}

export interface CategoriesEnvelope {
  categories: CategoryResponse[];
}

export interface TransactionsEnvelope {
  transactions: TransactionResponse[];
}

export interface GoalsEnvelope {
  goals: GoalResponse[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  phoneNumber: string;
}