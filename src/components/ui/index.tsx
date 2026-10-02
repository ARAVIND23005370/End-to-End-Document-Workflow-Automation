// ===========================
// E2EDocs — Reusable UI Components
// ===========================

import { useState, useRef, type ReactNode, type ButtonHTMLAttributes, type InputHTMLAttributes, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react';
import { Search, AlertCircle, CheckCircle, AlertTriangle, Info, X, ChevronLeft, ChevronRight, RefreshCw } from 'lucide-react';
import { cn } from '../../utils';
import { useClickOutside } from '../../hooks';

// --- Button ---
type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  icon?: boolean;
}

export function Button({ variant = 'primary', size = 'md', icon, className, children, ...props }: ButtonProps) {
  return (
    <button
      className={cn(
        'btn',
        `btn-${variant}`,
        size === 'sm' && 'btn-sm',
        size === 'lg' && 'btn-lg',
        icon && 'btn-icon',
        icon && size === 'sm' && 'btn-sm',
        className
      )}
      {...props}
    >
      {children}
    </button>
  );
}

// --- Badge ---
type BadgeVariant = 'neutral' | 'success' | 'warning' | 'error' | 'info' | 'brand';

interface BadgeProps {
  variant?: BadgeVariant;
  dot?: boolean;
  children: ReactNode;
  className?: string;
}

export function Badge({ variant = 'neutral', dot, children, className }: BadgeProps) {
  return (
    <span className={cn('badge', `badge-${variant}`, className)}>
      {dot && <span className="badge-dot" aria-hidden="true" />}
      {children}
    </span>
  );
}

// --- StatusBadge ---
const STATUS_BADGE_MAP: Record<string, { variant: BadgeVariant; label: string }> = {
  draft: { variant: 'neutral', label: 'Draft' },
  processing: { variant: 'info', label: 'Processing' },
  review: { variant: 'warning', label: 'In Review' },
  approved: { variant: 'success', label: 'Approved' },
  rejected: { variant: 'error', label: 'Rejected' },
  active: { variant: 'success', label: 'Active' },
  inactive: { variant: 'neutral', label: 'Inactive' },
  pending: { variant: 'warning', label: 'Pending' },
};

export function StatusBadge({ status }: { status: string }) {
  const config = STATUS_BADGE_MAP[status] || { variant: 'neutral' as BadgeVariant, label: status };
  return <Badge variant={config.variant} dot>{config.label}</Badge>;
}

// --- PriorityBadge ---
const PRIORITY_MAP: Record<string, { variant: BadgeVariant; label: string }> = {
  critical: { variant: 'error', label: 'Critical' },
  high: { variant: 'warning', label: 'High' },
  medium: { variant: 'info', label: 'Medium' },
  low: { variant: 'neutral', label: 'Low' },
};

export function PriorityBadge({ priority }: { priority: string }) {
  const config = PRIORITY_MAP[priority] || { variant: 'neutral' as BadgeVariant, label: priority };
  return <Badge variant={config.variant}>{config.label}</Badge>;
}

// --- Input ---
interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helper?: string;
}

export function Input({ label, error, helper, className, id, ...props }: InputProps) {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
  return (
    <div className="input-group">
      {label && <label className="input-label" htmlFor={inputId}>{label}</label>}
      <input
        id={inputId}
        className={cn('input-field', error && 'input-field-error', className)}
        aria-invalid={!!error}
        aria-describedby={error ? `${inputId}-error` : helper ? `${inputId}-helper` : undefined}
        {...props}
      />
      {error && <span id={`${inputId}-error`} className="input-error-text" role="alert">{error}</span>}
      {!error && helper && <span id={`${inputId}-helper`} className="input-helper">{helper}</span>}
    </div>
  );
}

// --- Select ---
interface SelectFieldProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  options: { value: string; label: string }[];
  placeholder?: string;
}

export function SelectField({ label, error, options, placeholder, className, id, ...props }: SelectFieldProps) {
  const selectId = id || label?.toLowerCase().replace(/\s+/g, '-');
  return (
    <div className="input-group">
      {label && <label className="input-label" htmlFor={selectId}>{label}</label>}
      <select
        id={selectId}
        className={cn('input-field', 'select-field', error && 'input-field-error', className)}
        aria-invalid={!!error}
        {...props}
      >
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>{opt.label}</option>
        ))}
      </select>
      {error && <span className="input-error-text" role="alert">{error}</span>}
    </div>
  );
}

// --- Textarea ---
interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
}

export function Textarea({ label, error, className, id, ...props }: TextareaProps) {
  const textareaId = id || label?.toLowerCase().replace(/\s+/g, '-');
  return (
    <div className="input-group">
      {label && <label className="input-label" htmlFor={textareaId}>{label}</label>}
      <textarea
        id={textareaId}
        className={cn('input-field', 'textarea-field', error && 'input-field-error', className)}
        aria-invalid={!!error}
        {...props}
      />
      {error && <span className="input-error-text" role="alert">{error}</span>}
    </div>
  );
}

// --- SearchInput ---
interface SearchInputProps extends InputHTMLAttributes<HTMLInputElement> {
  onSearch?: (value: string) => void;
}

export function SearchInput({ onSearch, className, ...props }: SearchInputProps) {
  return (
    <div className="search-input-wrapper">
      <Search size={16} className="search-input-icon" aria-hidden="true" />
      <input
        type="search"
        className={cn('input-field', 'search-input', className)}
        onChange={(e) => onSearch?.(e.target.value)}
        aria-label="Search"
        {...props}
      />
    </div>
  );
}

// --- Card ---
interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  children?: ReactNode;
  className?: string;
}

export function Card({ children, className, ...props }: CardProps) {
  return <div className={cn('card', className)} {...props}>{children}</div>;
}

export function CardHeader({ children, className, ...props }: CardProps) {
  return <div className={cn('card-header', className)} {...props}>{children}</div>;
}

export function CardBody({ children, className, ...props }: CardProps) {
  return <div className={cn('card-body', className)} {...props}>{children}</div>;
}

// --- Modal ---
interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  footer?: ReactNode;
}

export function Modal({ isOpen, onClose, title, children, footer }: ModalProps) {
  if (!isOpen) return null;
  return (
    <div className="modal-overlay" onClick={onClose} role="dialog" aria-modal="true" aria-label={title}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">{title}</h2>
          <Button variant="ghost" icon size="sm" onClick={onClose} aria-label="Close">
            <X size={16} />
          </Button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  );
}

// --- Confirm Dialog ---
interface ConfirmDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmLabel?: string;
  variant?: 'primary' | 'danger';
}

export function ConfirmDialog({ isOpen, onClose, onConfirm, title, message, confirmLabel = 'Confirm', variant = 'primary' }: ConfirmDialogProps) {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>Cancel</Button>
          <Button variant={variant === 'danger' ? 'danger' : 'primary'} onClick={onConfirm}>{confirmLabel}</Button>
        </>
      }
    >
      <p style={{ color: 'var(--text-secondary)', fontSize: 'var(--text-body-sm)' }}>{message}</p>
    </Modal>
  );
}

// --- Dropdown ---
interface DropdownProps {
  trigger: ReactNode;
  children: ReactNode;
  align?: 'left' | 'right';
}

export function Dropdown({ trigger, children, align = 'right' }: DropdownProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  useClickOutside(ref, () => setOpen(false));

  return (
    <div className="dropdown" ref={ref}>
      <div onClick={() => setOpen(!open)}>{trigger}</div>
      {open && (
        <div className="dropdown-menu" style={align === 'left' ? { left: 0, right: 'auto' } : undefined} onClick={() => setOpen(false)}>
          {children}
        </div>
      )}
    </div>
  );
}

export function DropdownItem({ children, onClick, className }: { children: ReactNode; onClick?: () => void; className?: string }) {
  return <button className={cn('dropdown-item', className)} onClick={onClick}>{children}</button>;
}

// --- Tabs ---
interface TabItem {
  id: string;
  label: string;
}

interface TabsProps {
  tabs: TabItem[];
  activeTab: string;
  onChange: (id: string) => void;
}

export function Tabs({ tabs, activeTab, onChange }: TabsProps) {
  return (
    <div className="tabs" role="tablist">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          className={cn('tab', activeTab === tab.id && 'tab-active')}
          role="tab"
          aria-selected={activeTab === tab.id}
          onClick={() => onChange(tab.id)}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}

// --- Toast ---
const TOAST_ICONS = {
  success: CheckCircle,
  warning: AlertTriangle,
  error: AlertCircle,
  info: Info,
};

interface ToastProps {
  type: 'success' | 'warning' | 'error' | 'info';
  message: string;
  onClose: () => void;
}

export function Toast({ type, message, onClose }: ToastProps) {
  const Icon = TOAST_ICONS[type];
  return (
    <div className={cn('toast', `toast-${type}`)} role="alert">
      <Icon size={18} style={{ flexShrink: 0, marginTop: 1 }} aria-hidden="true" />
      <span style={{ flex: 1, fontSize: 'var(--text-body-sm)' }}>{message}</span>
      <Button variant="ghost" icon size="sm" onClick={onClose} aria-label="Dismiss">
        <X size={14} />
      </Button>
    </div>
  );
}

// --- Skeleton --- 
export function Skeleton({ width, height, circle, className }: { width?: string | number; height?: string | number; circle?: boolean; className?: string }) {
  return (
    <div
      className={cn('skeleton', circle && 'skeleton-circle', className)}
      style={{ width: width || '100%', height: height || 14 }}
      aria-hidden="true"
    />
  );
}

export function SkeletonRow() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-4)', padding: 'var(--space-3) var(--space-4)' }}>
      <Skeleton width={80} height={14} />
      <Skeleton width="40%" height={14} />
      <Skeleton width={60} height={22} />
      <Skeleton width={60} height={14} />
      <Skeleton width={100} height={14} />
    </div>
  );
}

// --- Empty State ---
interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description: string;
  action?: ReactNode;
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="empty-state">
      {icon && <div className="empty-state-icon">{icon}</div>}
      <h3 className="empty-state-title">{title}</h3>
      <p className="empty-state-description">{description}</p>
      {action}
    </div>
  );
}

// --- Error State ---
interface ErrorStateProps {
  title?: string;
  message: string;
  onRetry?: () => void;
}

export function ErrorState({ title = 'Something went wrong', message, onRetry }: ErrorStateProps) {
  return (
    <div className="error-state">
      <AlertCircle size={40} style={{ color: 'var(--color-error-400)', marginBottom: 'var(--space-4)' }} aria-hidden="true" />
      <h3 className="error-state-title">{title}</h3>
      <p className="error-state-description">{message}</p>
      {onRetry && (
        <Button variant="secondary" onClick={onRetry}>
          <RefreshCw size={14} />
          Try Again
        </Button>
      )}
    </div>
  );
}

// --- Loading State ---
export function LoadingState({ message = 'Loading...' }: { message?: string }) {
  return (
    <div className="empty-state" aria-live="polite">
      <div style={{ marginBottom: 'var(--space-4)' }}>
        <img
          src="/brand/e2edocs-icon.png"
          alt="E2EDocs"
          style={{ width: 36, height: 36, objectFit: 'contain', opacity: 0.85 }}
        />
      </div>
      <p className="empty-state-description">{message}</p>
    </div>
  );
}

// --- Breadcrumb ---
interface BreadcrumbItem {
  label: string;
  href?: string;
}

export function Breadcrumb({ items }: { items: BreadcrumbItem[] }) {
  return (
    <nav className="breadcrumb" aria-label="Breadcrumb">
      {items.map((item, i) => (
        <span key={i} className={i === items.length - 1 ? 'breadcrumb-current' : 'breadcrumb-item'}>
          {i > 0 && <span className="breadcrumb-separator" aria-hidden="true"> / </span>}
          {item.href && i < items.length - 1 ? <a href={item.href}>{item.label}</a> : item.label}
        </span>
      ))}
    </nav>
  );
}

// --- Pagination ---
interface PaginationProps {
  page: number;
  pageSize: number;
  total: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ page, pageSize, total, onPageChange }: PaginationProps) {
  const totalPages = Math.ceil(total / pageSize);
  if (totalPages <= 1) return null;

  const start = (page - 1) * pageSize + 1;
  const end = Math.min(page * pageSize, total);

  return (
    <div className="pagination">
      <span>Showing {start}–{end} of {total}</span>
      <div className="pagination-controls">
        <button
          className="pagination-btn"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 1}
          aria-label="Previous page"
        >
          <ChevronLeft size={16} />
        </button>
        {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
          let pageNum: number;
          if (totalPages <= 5) {
            pageNum = i + 1;
          } else if (page <= 3) {
            pageNum = i + 1;
          } else if (page >= totalPages - 2) {
            pageNum = totalPages - 4 + i;
          } else {
            pageNum = page - 2 + i;
          }
          return (
            <button
              key={pageNum}
              className={cn('pagination-btn', page === pageNum && 'pagination-btn-active')}
              onClick={() => onPageChange(pageNum)}
              aria-label={`Page ${pageNum}`}
              aria-current={page === pageNum ? 'page' : undefined}
            >
              {pageNum}
            </button>
          );
        })}
        <button
          className="pagination-btn"
          onClick={() => onPageChange(page + 1)}
          disabled={page === totalPages}
          aria-label="Next page"
        >
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}

// --- Avatar ---
export function Avatar({ name, size = 'md' }: { name: string; size?: 'sm' | 'md' | 'lg' }) {
  const initials = name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  return (
    <span className={cn('avatar', `avatar-${size}`)} aria-label={name} title={name}>
      {initials}
    </span>
  );
}
