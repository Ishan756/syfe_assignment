import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { api, ApiError } from './lib/api';
import type {
  CategoryResponse,
  CategoryType,
  GoalResponse,
  MonthlyReportResponse,
  TransactionResponse,
  YearlyReportResponse,
} from './lib/types';

type AuthMode = 'login' | 'register';

type AuthFormState = {
  username: string;
  password: string;
  fullName: string;
  phoneNumber: string;
};

type TransactionFormState = {
  amount: string;
  date: string;
  category: string;
  description: string;
};

type GoalFormState = {
  goalName: string;
  targetAmount: string;
  targetDate: string;
  startDate: string;
};

type CategoryFormState = {
  name: string;
  type: CategoryType;
};

type FilterState = {
  startDate: string;
  endDate: string;
  categoryId: string;
};

type AlertState = {
  type: 'success' | 'error';
  message: string;
} | null;

const moneyFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 2,
});

const today = new Date();
const currentMonth = today.getMonth() + 1;
const currentYear = today.getFullYear();
const todayIso = today.toISOString().slice(0, 10);

function addDays(days: number) {
  const next = new Date();
  next.setDate(next.getDate() + days);
  return next.toISOString().slice(0, 10);
}

function formatMoney(value: number | undefined | null) {
  return moneyFormatter.format(value ?? 0);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(value));
}

function currencyDelta(value: number) {
  return value >= 0 ? `+${formatMoney(value)}` : formatMoney(value);
}

function panelClass() {
  return 'rounded-[28px] border border-slate-200/80 bg-white/85 p-5 shadow-soft backdrop-blur';
}

function labelClass() {
  return 'text-xs font-semibold uppercase tracking-[0.2em] text-slate-500';
}

function inputClass() {
  return 'mt-2 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-teal-500 focus:bg-white focus:ring-4 focus:ring-teal-500/10';
}

function buttonClass(variant: 'primary' | 'secondary' | 'ghost' = 'primary') {
  const base = 'inline-flex items-center justify-center rounded-2xl px-4 py-3 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50';

  if (variant === 'primary') {
    return `${base} bg-slate-950 text-white hover:bg-slate-800`;
  }

  if (variant === 'secondary') {
    return `${base} bg-teal-600 text-white hover:bg-teal-700`;
  }

  return `${base} border border-slate-200 bg-white text-slate-700 hover:bg-slate-50`;
}

function pillClass(active: boolean) {
  return active
    ? 'rounded-full bg-slate-950 px-3 py-1 text-xs font-semibold text-white'
    : 'rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600';
}

function summaryCard({
  title,
  value,
  tone,
}: {
  title: string;
  value: string;
  tone: 'slate' | 'teal' | 'amber';
}) {
  const toneClasses = {
    slate: 'from-slate-950 to-slate-800 text-white',
    teal: 'from-teal-600 to-cyan-600 text-white',
    amber: 'from-amber-500 to-orange-500 text-white',
  }[tone];

  return (
    <div className={`rounded-[28px] bg-gradient-to-br ${toneClasses} p-5 shadow-soft`}>
      <div className="text-xs font-semibold uppercase tracking-[0.25em] text-white/70">
        {title}
      </div>
      <div className="mt-4 text-3xl font-semibold tracking-tight">{value}</div>
    </div>
  );
}

function sectionTitle({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="mb-4">
      <h2 className="font-display text-xl font-semibold tracking-tight text-slate-950">
        {title}
      </h2>
      {subtitle ? <p className="mt-1 text-sm text-slate-500">{subtitle}</p> : null}
    </div>
  );
}

function progressColor(percent: number) {
  if (percent >= 100) return 'bg-emerald-500';
  if (percent >= 70) return 'bg-teal-500';
  if (percent >= 40) return 'bg-amber-500';
  return 'bg-slate-400';
}

function breakdownEntries(data: Record<string, number>) {
  return Object.entries(data).sort((a, b) => b[1] - a[1]);
}

function App() {
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [checkingSession, setCheckingSession] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [alert, setAlert] = useState<AlertState>(null);
  const [busy, setBusy] = useState(false);

  const [authForm, setAuthForm] = useState<AuthFormState>({
    username: '',
    password: '',
    fullName: '',
    phoneNumber: '',
  });
  const [transactionForm, setTransactionForm] = useState<TransactionFormState>({
    amount: '',
    date: todayIso,
    category: 'Salary',
    description: '',
  });
  const [goalForm, setGoalForm] = useState<GoalFormState>({
    goalName: '',
    targetAmount: '',
    targetDate: addDays(30),
    startDate: todayIso,
  });
  const [categoryForm, setCategoryForm] = useState<CategoryFormState>({
    name: '',
    type: 'EXPENSE',
  });
  const [filters, setFilters] = useState<FilterState>({
    startDate: '',
    endDate: '',
    categoryId: '',
  });
  const [reportYear, setReportYear] = useState(String(currentYear));

  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [goals, setGoals] = useState<GoalResponse[]>([]);
  const [monthlyReport, setMonthlyReport] = useState<MonthlyReportResponse | null>(null);
  const [yearlyReport, setYearlyReport] = useState<YearlyReportResponse | null>(null);

  const [editingTransactionId, setEditingTransactionId] = useState<number | null>(null);
  const [editingGoalId, setEditingGoalId] = useState<number | null>(null);

  const categoryOptions = useMemo(() => categories, [categories]);

  useEffect(() => {
    void bootstrap();
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      void loadReports(Number(reportYear));
    }
  }, [reportYear]);

  async function bootstrap() {
    try {
      await loadDashboard();
      setIsAuthenticated(true);
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        setIsAuthenticated(false);
      } else if (error instanceof Error) {
        setAlert({ type: 'error', message: error.message });
      }
    } finally {
      setCheckingSession(false);
    }
  }

  async function loadDashboard() {
    const [categoriesResponse, transactionsResponse, goalsResponse, monthlyResponse, yearlyResponse] =
      await Promise.all([
        api.getCategories(),
        api.getTransactions(),
        api.getGoals(),
        api.getMonthlyReport(currentYear, currentMonth),
        api.getYearlyReport(Number(reportYear)),
      ]);

    setCategories(categoriesResponse.categories);
    setTransactions(transactionsResponse.transactions);
    setGoals(goalsResponse.goals);
    setMonthlyReport(monthlyResponse);
    setYearlyReport(yearlyResponse);

    if (!transactionForm.category && categoriesResponse.categories.length > 0) {
      setTransactionForm((current) => ({
        ...current,
        category: categoriesResponse.categories[0].name,
      }));
    }
  }

  async function loadReports(year: number) {
    const [monthlyResponse, yearlyResponse] = await Promise.all([
      api.getMonthlyReport(year, currentMonth),
      api.getYearlyReport(year),
    ]);

    setMonthlyReport(monthlyResponse);
    setYearlyReport(yearlyResponse);
  }

  async function loadTransactions(nextFilters?: FilterState) {
    const response = await api.getTransactions({
      startDate: nextFilters?.startDate || undefined,
      endDate: nextFilters?.endDate || undefined,
      categoryId: nextFilters?.categoryId ? Number(nextFilters.categoryId) : undefined,
    });

    setTransactions(response.transactions);
  }

  async function reloadEverything() {
    await loadDashboard();
  }

  function showSuccess(message: string) {
    setAlert({ type: 'success', message });
  }

  function showError(message: string) {
    setAlert({ type: 'error', message });
  }

  async function handleAuthSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setAlert(null);

    try {
      if (authMode === 'login') {
        await api.login({ username: authForm.username, password: authForm.password });
        showSuccess('Logged in successfully.');
      } else {
        await api.register({
          username: authForm.username,
          password: authForm.password,
          fullName: authForm.fullName,
          phoneNumber: authForm.phoneNumber,
        });
        showSuccess('Account created. You can log in now.');
        setAuthMode('login');
      }

      setIsAuthenticated(true);
      await reloadEverything();
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleLogout() {
    setBusy(true);
    try {
      await api.logout();
      setIsAuthenticated(false);
      setCategories([]);
      setTransactions([]);
      setGoals([]);
      setMonthlyReport(null);
      setYearlyReport(null);
      setEditingTransactionId(null);
      setEditingGoalId(null);
      showSuccess('Logged out successfully.');
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleTransactionSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);

    try {
      const payload = {
        amount: Number(transactionForm.amount),
        date: transactionForm.date,
        category: transactionForm.category,
        description: transactionForm.description || undefined,
      };

      if (editingTransactionId) {
        await api.updateTransaction(editingTransactionId, {
          amount: payload.amount,
          category: payload.category,
          description: payload.description,
        });
        showSuccess('Transaction updated.');
      } else {
        await api.createTransaction(payload);
        showSuccess('Transaction added.');
      }

      setTransactionForm((current) => ({
        ...current,
        amount: '',
        description: '',
      }));
      setEditingTransactionId(null);
      await reloadEverything();
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  function startEditTransaction(transaction: TransactionResponse) {
    setEditingTransactionId(transaction.id);
    setTransactionForm({
      amount: String(transaction.amount),
      date: transaction.date,
      category: transaction.category,
      description: transaction.description || '',
    });
  }

  async function deleteTransaction(id: number) {
    setBusy(true);
    try {
      await api.deleteTransaction(id);
      showSuccess('Transaction deleted.');
      await reloadEverything();
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleCategorySubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);

    try {
      await api.createCategory(categoryForm);
      setCategoryForm({ name: '', type: 'EXPENSE' });
      showSuccess('Category created.');
      await reloadEverything();
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  async function deleteCategory(name: string) {
    setBusy(true);
    try {
      await api.deleteCategory(name);
      showSuccess('Category deleted.');
      await reloadEverything();
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  async function handleGoalSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);

    try {
      const payload = {
        goalName: goalForm.goalName,
        targetAmount: Number(goalForm.targetAmount),
        targetDate: goalForm.targetDate,
        startDate: goalForm.startDate || undefined,
      };

      if (editingGoalId) {
        await api.updateGoal(editingGoalId, {
          targetAmount: payload.targetAmount,
          targetDate: payload.targetDate,
        });
        showSuccess('Goal updated.');
      } else {
        await api.createGoal(payload);
        showSuccess('Goal created.');
      }

      setGoalForm({
        goalName: '',
        targetAmount: '',
        targetDate: addDays(30),
        startDate: todayIso,
      });
      setEditingGoalId(null);
      await reloadEverything();
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  function startEditGoal(goal: GoalResponse) {
    setEditingGoalId(goal.id);
    setGoalForm({
      goalName: goal.goalName,
      targetAmount: String(goal.targetAmount),
      targetDate: goal.targetDate,
      startDate: goal.startDate,
    });
  }

  async function deleteGoal(id: number) {
    setBusy(true);
    try {
      await api.deleteGoal(id);
      showSuccess('Goal deleted.');
      await reloadEverything();
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  async function applyTransactionFilters(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);

    try {
      await loadTransactions(filters);
      showSuccess('Filters applied.');
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      }
    } finally {
      setBusy(false);
    }
  }

  const monthlyIncome = monthlyReport
    ? Object.values(monthlyReport.totalIncome).reduce((sum, value) => sum + value, 0)
    : 0;
  const monthlyExpenses = monthlyReport
    ? Object.values(monthlyReport.totalExpenses).reduce((sum, value) => sum + value, 0)
    : 0;
  const monthlyNet = monthlyReport?.netSavings ?? 0;

  const yearlyIncome = yearlyReport
    ? Object.values(yearlyReport.totalIncome).reduce((sum, value) => sum + value, 0)
    : 0;
  const yearlyExpenses = yearlyReport
    ? Object.values(yearlyReport.totalExpenses).reduce((sum, value) => sum + value, 0)
    : 0;
  const yearlyNet = yearlyReport?.netSavings ?? 0;

  if (checkingSession) {
    return (
      <div className="min-h-screen px-6 py-10 text-slate-900 sm:px-8 lg:px-10">
        <div className="mx-auto flex min-h-[70vh] max-w-7xl items-center justify-center">
          <div className={panelClass()}>
            <div className="font-display text-2xl font-semibold">Loading dashboard...</div>
            <p className="mt-2 text-sm text-slate-500">
              Checking your session and preparing the finance workspace.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="relative min-h-screen overflow-hidden px-4 py-6 text-slate-900 sm:px-6 lg:px-8">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(20,184,166,0.12),transparent_30%),radial-gradient(circle_at_top_left,rgba(245,158,11,0.12),transparent_28%)]" />
      <div className="relative mx-auto flex max-w-7xl flex-col gap-6">
        {alert ? (
          <div
            className={`rounded-3xl border px-4 py-3 text-sm shadow-soft ${
              alert.type === 'success'
                ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
                : 'border-rose-200 bg-rose-50 text-rose-800'
            }`}
          >
            {alert.message}
          </div>
        ) : null}

        <header className="grid gap-6 lg:grid-cols-[1.5fr_0.9fr]">
          <section className="rounded-[34px] border border-slate-200/80 bg-white/85 p-7 shadow-soft backdrop-blur">
            <div className="flex flex-wrap items-center gap-2 text-xs font-semibold uppercase tracking-[0.3em] text-teal-700">
              <span className="rounded-full bg-teal-50 px-3 py-1">React + Tailwind</span>
              <span className="rounded-full bg-amber-50 px-3 py-1">Session auth</span>
              <span className="rounded-full bg-slate-50 px-3 py-1">Spring Boot backend</span>
            </div>
            <h1 className="mt-6 max-w-3xl font-display text-4xl font-semibold tracking-tight text-slate-950 sm:text-5xl">
              A simple finance workspace for transactions, goals, and reports.
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600">
              This frontend stays intentionally quiet so the backend does the heavy lifting.
              Log in, manage your money, and inspect the monthly or yearly view without extra noise.
            </p>

            <div className="mt-8 grid gap-4 sm:grid-cols-3">
              {summaryCard({ title: 'Monthly income', value: formatMoney(monthlyIncome), tone: 'teal' })}
              {summaryCard({ title: 'Monthly expenses', value: formatMoney(monthlyExpenses), tone: 'amber' })}
              {summaryCard({ title: 'Monthly net', value: currencyDelta(monthlyNet), tone: 'slate' })}
            </div>
          </section>

          <section className={panelClass()}>
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className={labelClass()}>Workspace</div>
                <h2 className="mt-2 font-display text-2xl font-semibold tracking-tight text-slate-950">
                  {isAuthenticated ? 'Dashboard ready' : 'Sign in to continue'}
                </h2>
              </div>
              {isAuthenticated ? (
                <button className={buttonClass('ghost')} onClick={handleLogout} disabled={busy}>
                  Logout
                </button>
              ) : null}
            </div>

            {!isAuthenticated ? (
              <form className="mt-6 space-y-4" onSubmit={handleAuthSubmit}>
                <div className="flex gap-2 rounded-2xl bg-slate-100 p-1">
                  <button
                    type="button"
                    className={pillClass(authMode === 'login')}
                    onClick={() => setAuthMode('login')}
                  >
                    Login
                  </button>
                  <button
                    type="button"
                    className={pillClass(authMode === 'register')}
                    onClick={() => setAuthMode('register')}
                  >
                    Register
                  </button>
                </div>

                <label className="block">
                  <span className={labelClass()}>Email</span>
                  <input
                    className={inputClass()}
                    type="email"
                    value={authForm.username}
                    onChange={(event) =>
                      setAuthForm((current) => ({ ...current, username: event.target.value }))
                    }
                    placeholder="you@example.com"
                    required
                  />
                </label>

                <label className="block">
                  <span className={labelClass()}>Password</span>
                  <input
                    className={inputClass()}
                    type="password"
                    value={authForm.password}
                    onChange={(event) =>
                      setAuthForm((current) => ({ ...current, password: event.target.value }))
                    }
                    placeholder="••••••••"
                    required
                  />
                </label>

                {authMode === 'register' ? (
                  <>
                    <label className="block">
                      <span className={labelClass()}>Full name</span>
                      <input
                        className={inputClass()}
                        value={authForm.fullName}
                        onChange={(event) =>
                          setAuthForm((current) => ({ ...current, fullName: event.target.value }))
                        }
                        placeholder="John Doe"
                        required
                      />
                    </label>

                    <label className="block">
                      <span className={labelClass()}>Phone number</span>
                      <input
                        className={inputClass()}
                        value={authForm.phoneNumber}
                        onChange={(event) =>
                          setAuthForm((current) => ({ ...current, phoneNumber: event.target.value }))
                        }
                        placeholder="+1 555 123 4567"
                        required
                      />
                    </label>
                  </>
                ) : null}

                <button className={buttonClass('secondary')} type="submit" disabled={busy}>
                  {authMode === 'login' ? 'Login' : 'Create account'}
                </button>
              </form>
            ) : (
              <div className="mt-6 space-y-4 text-sm text-slate-600">
                <div className="rounded-2xl bg-slate-50 p-4">
                  Session is active. You can create transactions, goals, and categories below.
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="rounded-2xl border border-slate-200 p-4">
                    <div className="font-semibold text-slate-900">Transactions</div>
                    <p className="mt-1">Add income or expenses and keep the list filtered.</p>
                  </div>
                  <div className="rounded-2xl border border-slate-200 p-4">
                    <div className="font-semibold text-slate-900">Goals</div>
                    <p className="mt-1">Track target amounts and progress in one view.</p>
                  </div>
                </div>
              </div>
            )}
          </section>
        </header>

        {isAuthenticated ? (
          <main className="grid gap-6 lg:grid-cols-[1.35fr_0.95fr]">
            <div className="space-y-6">
              <section className={panelClass()}>
                {sectionTitle({
                  title: editingTransactionId ? 'Edit transaction' : 'New transaction',
                  subtitle: 'Record money movements and keep the backend as the source of truth.',
                })}
                <form className="grid gap-4 md:grid-cols-2" onSubmit={handleTransactionSubmit}>
                  <label className="block">
                    <span className={labelClass()}>Amount</span>
                    <input
                      className={inputClass()}
                      type="number"
                      step="0.01"
                      min="0"
                      value={transactionForm.amount}
                      onChange={(event) =>
                        setTransactionForm((current) => ({
                          ...current,
                          amount: event.target.value,
                        }))
                      }
                      placeholder="250.00"
                      required
                    />
                  </label>

                  <label className="block">
                    <span className={labelClass()}>Date</span>
                    <input
                      className={inputClass()}
                      type="date"
                      value={transactionForm.date}
                      onChange={(event) =>
                        setTransactionForm((current) => ({
                          ...current,
                          date: event.target.value,
                        }))
                      }
                      required
                    />
                  </label>

                  <label className="block">
                    <span className={labelClass()}>Category</span>
                    <select
                      className={inputClass()}
                      value={transactionForm.category}
                      onChange={(event) =>
                        setTransactionForm((current) => ({
                          ...current,
                          category: event.target.value,
                        }))
                      }
                      required
                    >
                      {categoryOptions.map((category) => (
                        <option key={category.name} value={category.name}>
                          {category.name} ({category.type})
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="block">
                    <span className={labelClass()}>Description</span>
                    <input
                      className={inputClass()}
                      value={transactionForm.description}
                      onChange={(event) =>
                        setTransactionForm((current) => ({
                          ...current,
                          description: event.target.value,
                        }))
                      }
                      placeholder="Short note"
                    />
                  </label>

                  <div className="flex flex-wrap gap-3 md:col-span-2">
                    <button className={buttonClass('secondary')} type="submit" disabled={busy}>
                      {editingTransactionId ? 'Update transaction' : 'Add transaction'}
                    </button>
                    {editingTransactionId ? (
                      <button
                        className={buttonClass('ghost')}
                        type="button"
                        onClick={() => {
                          setEditingTransactionId(null);
                          setTransactionForm({
                            amount: '',
                            date: todayIso,
                            category: categoryOptions[0]?.name || 'Salary',
                            description: '',
                          });
                        }}
                      >
                        Cancel edit
                      </button>
                    ) : null}
                  </div>
                </form>
              </section>

              <section className={panelClass()}>
                <div className="flex flex-wrap items-end justify-between gap-3">
                  {sectionTitle({
                    title: 'Transactions',
                    subtitle: 'Filter, edit, or delete anything from the recent ledger.',
                  })}
                  <form className="grid gap-3 md:grid-cols-3" onSubmit={applyTransactionFilters}>
                    <input
                      className={inputClass()}
                      type="date"
                      value={filters.startDate}
                      onChange={(event) =>
                        setFilters((current) => ({ ...current, startDate: event.target.value }))
                      }
                      placeholder="Start date"
                    />
                    <input
                      className={inputClass()}
                      type="date"
                      value={filters.endDate}
                      onChange={(event) =>
                        setFilters((current) => ({ ...current, endDate: event.target.value }))
                      }
                      placeholder="End date"
                    />
                    <select
                      className={inputClass()}
                      value={filters.categoryId}
                      onChange={(event) =>
                        setFilters((current) => ({ ...current, categoryId: event.target.value }))
                      }
                    >
                      <option value="">All categories</option>
                      {categoryOptions.map((category) => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                    <div className="md:col-span-3 flex gap-3">
                      <button className={buttonClass('secondary')} type="submit" disabled={busy}>
                        Apply filters
                      </button>
                      <button
                        className={buttonClass('ghost')}
                        type="button"
                        onClick={() => {
                          setFilters({ startDate: '', endDate: '', categoryId: '' });
                          void loadTransactions();
                        }}
                        disabled={busy}
                      >
                        Reset
                      </button>
                    </div>
                  </form>
                </div>

                <div className="mt-5 overflow-hidden rounded-[24px] border border-slate-200">
                  <table className="min-w-full divide-y divide-slate-200 text-left text-sm">
                    <thead className="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-500">
                      <tr>
                        <th className="px-4 py-3">Date</th>
                        <th className="px-4 py-3">Category</th>
                        <th className="px-4 py-3">Type</th>
                        <th className="px-4 py-3">Description</th>
                        <th className="px-4 py-3 text-right">Amount</th>
                        <th className="px-4 py-3 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 bg-white">
                      {transactions.length > 0 ? (
                        transactions.map((transaction) => (
                          <tr key={transaction.id} className="align-top">
                            <td className="px-4 py-4 text-slate-600">{formatDate(transaction.date)}</td>
                            <td className="px-4 py-4 font-semibold text-slate-950">{transaction.category}</td>
                            <td className="px-4 py-4">
                              <span
                                className={`rounded-full px-3 py-1 text-xs font-semibold ${
                                  transaction.type === 'INCOME'
                                    ? 'bg-emerald-50 text-emerald-700'
                                    : 'bg-rose-50 text-rose-700'
                                }`}
                              >
                                {transaction.type}
                              </span>
                            </td>
                            <td className="px-4 py-4 text-slate-600">
                              {transaction.description || '—'}
                            </td>
                            <td className="px-4 py-4 text-right font-semibold text-slate-950">
                              {formatMoney(transaction.amount)}
                            </td>
                            <td className="px-4 py-4 text-right">
                              <div className="inline-flex gap-2">
                                <button
                                  className={buttonClass('ghost')}
                                  type="button"
                                  onClick={() => startEditTransaction(transaction)}
                                >
                                  Edit
                                </button>
                                <button
                                  className={buttonClass('ghost')}
                                  type="button"
                                  onClick={() => deleteTransaction(transaction.id)}
                                >
                                  Delete
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td className="px-4 py-8 text-center text-slate-500" colSpan={6}>
                            No transactions yet. Add one above to start tracking.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </section>
            </div>

            <aside className="space-y-6">
              <section className={panelClass()}>
                {sectionTitle({
                  title: 'Savings goals',
                  subtitle: 'A compact view of target amounts and progress.',
                })}
                <form className="space-y-4" onSubmit={handleGoalSubmit}>
                  <label className="block">
                    <span className={labelClass()}>Goal name</span>
                    <input
                      className={inputClass()}
                      value={goalForm.goalName}
                      onChange={(event) =>
                        setGoalForm((current) => ({ ...current, goalName: event.target.value }))
                      }
                      placeholder="Emergency fund"
                      required
                    />
                  </label>

                  <div className="grid gap-4 sm:grid-cols-2">
                    <label className="block">
                      <span className={labelClass()}>Target amount</span>
                      <input
                        className={inputClass()}
                        type="number"
                        min="0"
                        step="0.01"
                        value={goalForm.targetAmount}
                        onChange={(event) =>
                          setGoalForm((current) => ({ ...current, targetAmount: event.target.value }))
                        }
                        required
                      />
                    </label>

                    <label className="block">
                      <span className={labelClass()}>Target date</span>
                      <input
                        className={inputClass()}
                        type="date"
                        value={goalForm.targetDate}
                        onChange={(event) =>
                          setGoalForm((current) => ({ ...current, targetDate: event.target.value }))
                        }
                        required
                      />
                    </label>
                  </div>

                  <label className="block">
                    <span className={labelClass()}>Start date</span>
                    <input
                      className={inputClass()}
                      type="date"
                      value={goalForm.startDate}
                      onChange={(event) =>
                        setGoalForm((current) => ({ ...current, startDate: event.target.value }))
                      }
                    />
                  </label>

                  <div className="flex flex-wrap gap-3">
                    <button className={buttonClass('secondary')} type="submit" disabled={busy}>
                      {editingGoalId ? 'Update goal' : 'Save goal'}
                    </button>
                    {editingGoalId ? (
                      <button
                        className={buttonClass('ghost')}
                        type="button"
                        onClick={() => {
                          setEditingGoalId(null);
                          setGoalForm({
                            goalName: '',
                            targetAmount: '',
                            targetDate: addDays(30),
                            startDate: todayIso,
                          });
                        }}
                      >
                        Cancel edit
                      </button>
                    ) : null}
                  </div>
                </form>

                <div className="mt-5 space-y-3">
                  {goals.length > 0 ? (
                    goals.map((goal) => (
                      <div key={goal.id} className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                        <div className="flex items-start justify-between gap-4">
                          <div>
                            <div className="font-semibold text-slate-950">{goal.goalName}</div>
                            <div className="mt-1 text-xs text-slate-500">
                              Target {formatMoney(goal.targetAmount)} by {formatDate(goal.targetDate)}
                            </div>
                          </div>
                          <div className="text-right text-sm font-semibold text-slate-950">
                            {goal.progressPercentage}%
                          </div>
                        </div>

                        <div className="mt-3 h-2 overflow-hidden rounded-full bg-slate-200">
                          <div
                            className={`h-full rounded-full ${progressColor(goal.progressPercentage)}`}
                            style={{ width: `${Math.min(goal.progressPercentage, 100)}%` }}
                          />
                        </div>

                        <div className="mt-3 grid grid-cols-2 gap-3 text-xs text-slate-600">
                          <div>
                            <div className="text-slate-500">Progress</div>
                            <div className="font-semibold text-slate-950">{formatMoney(goal.currentProgress)}</div>
                          </div>
                          <div>
                            <div className="text-slate-500">Remaining</div>
                            <div className="font-semibold text-slate-950">{formatMoney(goal.remainingAmount)}</div>
                          </div>
                        </div>

                        <div className="mt-4 flex gap-2">
                          <button className={buttonClass('ghost')} type="button" onClick={() => startEditGoal(goal)}>
                            Edit
                          </button>
                          <button className={buttonClass('ghost')} type="button" onClick={() => deleteGoal(goal.id)}>
                            Delete
                          </button>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="rounded-3xl border border-dashed border-slate-300 bg-slate-50 p-5 text-sm text-slate-500">
                      No goals yet. Create one to track savings momentum.
                    </div>
                  )}
                </div>
              </section>

              <section className={panelClass()}>
                {sectionTitle({
                  title: 'Categories',
                  subtitle: 'Default categories are seeded by the backend.',
                })}
                <form className="grid gap-4 sm:grid-cols-[1fr_auto]" onSubmit={handleCategorySubmit}>
                  <label className="block sm:col-span-2">
                    <span className={labelClass()}>Category name</span>
                    <input
                      className={inputClass()}
                      value={categoryForm.name}
                      onChange={(event) =>
                        setCategoryForm((current) => ({ ...current, name: event.target.value }))
                      }
                      placeholder="Travel"
                      required
                    />
                  </label>
                  <label className="block">
                    <span className={labelClass()}>Type</span>
                    <select
                      className={inputClass()}
                      value={categoryForm.type}
                      onChange={(event) =>
                        setCategoryForm((current) => ({
                          ...current,
                          type: event.target.value as CategoryType,
                        }))
                      }
                    >
                      <option value="EXPENSE">Expense</option>
                      <option value="INCOME">Income</option>
                    </select>
                  </label>
                  <div className="self-end">
                    <button className={buttonClass('secondary')} type="submit" disabled={busy}>
                      Add category
                    </button>
                  </div>
                </form>

                <div className="mt-5 flex flex-wrap gap-2">
                  {categories.length > 0 ? (
                    categories.map((category) => (
                      <div
                        key={category.name}
                        className="flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-2 text-sm"
                      >
                        <span className="font-semibold text-slate-950">{category.name}</span>
                        <span className="text-xs uppercase tracking-[0.18em] text-slate-500">
                          {category.type}
                        </span>
                        {category.isCustom ? (
                          <button
                            className="text-xs font-semibold text-rose-600"
                            type="button"
                            onClick={() => deleteCategory(category.name)}
                          >
                            Remove
                          </button>
                        ) : null}
                      </div>
                    ))
                  ) : (
                    <div className="text-sm text-slate-500">Loading categories...</div>
                  )}
                </div>
              </section>

              <section className={panelClass()}>
                <div className="flex items-center justify-between gap-3">
                  {sectionTitle({
                    title: 'Reports',
                    subtitle: 'A compact summary of your backend-generated totals.',
                  })}
                  <input
                    className="w-28 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm outline-none focus:border-teal-500 focus:ring-4 focus:ring-teal-500/10"
                    type="number"
                    value={reportYear}
                    onChange={(event) => setReportYear(event.target.value)}
                  />
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="rounded-3xl bg-slate-950 p-4 text-white">
                    <div className="text-xs uppercase tracking-[0.2em] text-white/60">Monthly net</div>
                    <div className="mt-2 text-2xl font-semibold">{formatMoney(monthlyNet)}</div>
                    <div className="mt-4 text-sm text-white/70">
                      {currentYear} / {String(currentMonth).padStart(2, '0')}
                    </div>
                  </div>
                  <div className="rounded-3xl bg-teal-600 p-4 text-white">
                    <div className="text-xs uppercase tracking-[0.2em] text-white/60">Yearly net</div>
                    <div className="mt-2 text-2xl font-semibold">{formatMoney(yearlyNet)}</div>
                    <div className="mt-4 text-sm text-white/70">Year {reportYear}</div>
                  </div>
                </div>

                <div className="mt-5 grid gap-5 md:grid-cols-2">
                  <div className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                    <div className="font-semibold text-slate-950">Monthly breakdown</div>
                    <div className="mt-3 space-y-2 text-sm">
                      {monthlyReport ? (
                        <>
                          {breakdownEntries(monthlyReport.totalIncome).map(([name, value]) => (
                            <div key={`mi-${name}`} className="flex items-center justify-between">
                              <span className="text-slate-600">Income · {name}</span>
                              <span className="font-semibold text-emerald-700">{formatMoney(value)}</span>
                            </div>
                          ))}
                          {breakdownEntries(monthlyReport.totalExpenses).map(([name, value]) => (
                            <div key={`me-${name}`} className="flex items-center justify-between">
                              <span className="text-slate-600">Expense · {name}</span>
                              <span className="font-semibold text-rose-700">{formatMoney(value)}</span>
                            </div>
                          ))}
                          {!Object.keys(monthlyReport.totalIncome).length && !Object.keys(monthlyReport.totalExpenses).length ? (
                            <div className="text-slate-500">No report data yet.</div>
                          ) : null}
                        </>
                      ) : (
                        <div className="text-slate-500">Loading monthly report...</div>
                      )}
                    </div>
                  </div>

                  <div className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                    <div className="font-semibold text-slate-950">Yearly breakdown</div>
                    <div className="mt-3 space-y-2 text-sm">
                      {yearlyReport ? (
                        <>
                          {breakdownEntries(yearlyReport.totalIncome).map(([name, value]) => (
                            <div key={`yi-${name}`} className="flex items-center justify-between">
                              <span className="text-slate-600">Income · {name}</span>
                              <span className="font-semibold text-emerald-700">{formatMoney(value)}</span>
                            </div>
                          ))}
                          {breakdownEntries(yearlyReport.totalExpenses).map(([name, value]) => (
                            <div key={`ye-${name}`} className="flex items-center justify-between">
                              <span className="text-slate-600">Expense · {name}</span>
                              <span className="font-semibold text-rose-700">{formatMoney(value)}</span>
                            </div>
                          ))}
                          {!Object.keys(yearlyReport.totalIncome).length && !Object.keys(yearlyReport.totalExpenses).length ? (
                            <div className="text-slate-500">No report data yet.</div>
                          ) : null}
                        </>
                      ) : (
                        <div className="text-slate-500">Loading yearly report...</div>
                      )}
                    </div>
                  </div>
                </div>
              </section>
            </aside>
          </main>
        ) : (
          <section className={panelClass()}>
            {sectionTitle({
              title: 'What this frontend covers',
              subtitle: 'A lightweight layer over the REST API, with no extra opinionated workflow.',
            })}
            <div className="grid gap-4 md:grid-cols-3">
              <div className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                <div className="font-semibold text-slate-950">Authentication</div>
                <p className="mt-2 text-sm leading-6 text-slate-600">
                  Register or log in with the session-based API and reuse the same cookie in the
                  rest of the dashboard.
                </p>
              </div>
              <div className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                <div className="font-semibold text-slate-950">Core data</div>
                <p className="mt-2 text-sm leading-6 text-slate-600">
                  Create transactions, categories, and savings goals without leaving the page.
                </p>
              </div>
              <div className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                <div className="font-semibold text-slate-950">Reports</div>
                <p className="mt-2 text-sm leading-6 text-slate-600">
                  Compare monthly and yearly totals with a minimal summary view.
                </p>
              </div>
            </div>
          </section>
        )}
      </div>
    </div>
  );
}

export default App;