import { PublicShell } from "../_components/Shell";

export default function PublicLayout({ children }: { children: React.ReactNode }) {
    return <PublicShell>{children}</PublicShell>;
}

