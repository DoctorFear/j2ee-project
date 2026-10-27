import type { ReactNode } from "react";

interface PageHeadingProps {
  title: ReactNode;
  description: ReactNode;
  eyebrow?: ReactNode;
  actions?: ReactNode;
  className?: string;
  titleId?: string;
}

export function PageHeading({ title, description, eyebrow, actions, className, titleId }: PageHeadingProps) {
  return (
    <div className={className}>
      <div>
        {eyebrow}
        <h1 id={titleId}>{title}</h1>
        <p>{description}</p>
      </div>
      {actions}
    </div>
  );
}
