"use client";

import { useParams } from "next/navigation";
import ModuleForm from "../../ModuleForm";

export default function EditModulePage() {
  const params = useParams();
  return <ModuleForm moduleId={String(params.id)} />;
}
