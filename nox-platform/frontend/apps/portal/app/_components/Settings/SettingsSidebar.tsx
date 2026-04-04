'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

interface SettingsNavItem {
  label: string;
  href: string;
  icon?: React.ReactNode;
}

interface SettingsSidebarProps {
  items: SettingsNavItem[];
  baseHref: string;
}

export function SettingsSidebar({ items, baseHref }: SettingsSidebarProps) {
  const pathname = usePathname();

  return (
    <aside className="w-full md:w-48 md:border-r md:border-[rgb(var(--border))] md:pr-6">
      <nav className="flex flex-row md:flex-col gap-2 overflow-x-auto md:overflow-x-visible">
        {items.map((item) => {
          const href = `${baseHref}${item.href}`;
          const isActive = pathname === href || pathname.startsWith(`${href}/`);

          return (
            <Link
              key={item.href}
              href={href}
              className={[
                'flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all whitespace-nowrap md:whitespace-normal',
                isActive
                  ? 'bg-[rgb(var(--accent))] text-[rgb(var(--accent-foreground))] shadow-sm'
                  : 'text-[rgb(var(--text-main))] hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--accent))]',
              ].join(' ')}
            >
              {item.icon && <span className="h-4 w-4">{item.icon}</span>}
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
