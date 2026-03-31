CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tenants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  firebase_uid TEXT UNIQUE,
  status TEXT NOT NULL DEFAULT 'active',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_users_tenant_created_at ON users (tenant_id, created_at);

CREATE TABLE IF NOT EXISTS user_profiles (
  user_id UUID PRIMARY KEY REFERENCES users(id),
  legal_first_name TEXT,
  legal_last_name TEXT,
  date_of_birth DATE,
  nationality_country_code TEXT,
  current_country_code TEXT
);

CREATE TABLE IF NOT EXISTS user_channels (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  user_id UUID NOT NULL REFERENCES users(id),
  channel_type TEXT NOT NULL,
  channel_value TEXT NOT NULL,
  is_primary BOOLEAN NOT NULL DEFAULT FALSE,
  verified_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, channel_type, channel_value)
);

CREATE INDEX IF NOT EXISTS idx_user_channels_user ON user_channels (tenant_id, user_id);

CREATE TABLE IF NOT EXISTS principals (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  principal_type TEXT NOT NULL,
  email TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_principals_tenant_type ON principals (tenant_id, principal_type);

CREATE TABLE IF NOT EXISTS roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  name TEXT NOT NULL,
  UNIQUE (tenant_id, name)
);

CREATE TABLE IF NOT EXISTS principal_roles (
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  principal_id UUID NOT NULL REFERENCES principals(id),
  role_id UUID NOT NULL REFERENCES roles(id),
  scope_type TEXT NOT NULL,
  scope_id UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (tenant_id, principal_id, role_id, scope_type, scope_id)
);

CREATE INDEX IF NOT EXISTS idx_principal_roles_principal ON principal_roles (tenant_id, principal_id);

CREATE TABLE IF NOT EXISTS service_types (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  code TEXT NOT NULL,
  name TEXT NOT NULL,
  version INT NOT NULL DEFAULT 1,
  config JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, code, version)
);

CREATE TABLE IF NOT EXISTS service_instances (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  user_id UUID NOT NULL REFERENCES users(id),
  service_type_id UUID NOT NULL REFERENCES service_types(id),
  status TEXT NOT NULL DEFAULT 'active',
  assignee_principal_id UUID REFERENCES principals(id),
  opened_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  closed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_service_instances_user_status ON service_instances (tenant_id, user_id, status);
CREATE INDEX IF NOT EXISTS idx_service_instances_type_status ON service_instances (tenant_id, service_type_id, status);
CREATE INDEX IF NOT EXISTS idx_service_instances_assignee_status ON service_instances (tenant_id, assignee_principal_id, status);

CREATE TABLE IF NOT EXISTS service_instance_attributes (
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  service_instance_id UUID NOT NULL REFERENCES service_instances(id),
  key TEXT NOT NULL,
  value_text TEXT,
  value_number NUMERIC,
  value_bool BOOLEAN,
  value_date DATE,
  value_json JSONB,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (tenant_id, service_instance_id, key)
);

CREATE INDEX IF NOT EXISTS idx_service_instance_attributes_key_text ON service_instance_attributes (tenant_id, key, value_text);
CREATE INDEX IF NOT EXISTS idx_service_instance_attributes_key_number ON service_instance_attributes (tenant_id, key, value_number);
CREATE INDEX IF NOT EXISTS idx_service_instance_attributes_value_json_gin ON service_instance_attributes USING GIN (value_json);

CREATE TABLE IF NOT EXISTS workflow_step_types (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  service_type_id UUID NOT NULL REFERENCES service_types(id),
  code TEXT NOT NULL,
  name TEXT NOT NULL,
  sort_order INT NOT NULL,
  UNIQUE (tenant_id, service_type_id, code)
);

CREATE TABLE IF NOT EXISTS workflow_step_completions (
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  service_instance_id UUID NOT NULL REFERENCES service_instances(id),
  step_type_id UUID NOT NULL REFERENCES workflow_step_types(id),
  completed_at TIMESTAMPTZ NOT NULL,
  completed_by_principal_id UUID REFERENCES principals(id),
  PRIMARY KEY (tenant_id, service_instance_id, step_type_id)
);

CREATE INDEX IF NOT EXISTS idx_workflow_step_completions_instance_completed_at ON workflow_step_completions (tenant_id, service_instance_id, completed_at DESC);

CREATE TABLE IF NOT EXISTS document_types (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  code TEXT NOT NULL,
  name TEXT NOT NULL,
  config JSONB NOT NULL DEFAULT '{}'::jsonb,
  UNIQUE (tenant_id, code)
);

CREATE TABLE IF NOT EXISTS service_document_requirements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  service_type_id UUID NOT NULL REFERENCES service_types(id),
  document_type_id UUID NOT NULL REFERENCES document_types(id),
  is_required BOOLEAN NOT NULL DEFAULT TRUE,
  rules JSONB NOT NULL DEFAULT '{}'::jsonb,
  UNIQUE (tenant_id, service_type_id, document_type_id)
);

CREATE TABLE IF NOT EXISTS document_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  service_instance_id UUID NOT NULL REFERENCES service_instances(id),
  document_type_id UUID NOT NULL REFERENCES document_types(id),
  status TEXT NOT NULL DEFAULT 'pending',
  last_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, service_instance_id, document_type_id)
);

CREATE INDEX IF NOT EXISTS idx_document_requests_instance_status ON document_requests (tenant_id, service_instance_id, status);

CREATE TABLE IF NOT EXISTS document_submissions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  document_request_id UUID NOT NULL REFERENCES document_requests(id),
  submitted_by_principal_id UUID REFERENCES principals(id),
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_document_submissions_request_submitted_at ON document_submissions (tenant_id, document_request_id, submitted_at DESC);

CREATE TABLE IF NOT EXISTS document_files (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  submission_id UUID NOT NULL REFERENCES document_submissions(id),
  storage_provider TEXT NOT NULL,
  storage_path TEXT NOT NULL,
  sha256 TEXT,
  content_type TEXT,
  size_bytes BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, storage_path)
);

CREATE INDEX IF NOT EXISTS idx_document_files_submission ON document_files (tenant_id, submission_id);

CREATE TABLE IF NOT EXISTS document_review_actions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  document_request_id UUID NOT NULL REFERENCES document_requests(id),
  submission_id UUID REFERENCES document_submissions(id),
  action_type TEXT NOT NULL,
  verdict TEXT,
  feedback TEXT,
  actor_principal_id UUID NOT NULL REFERENCES principals(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_document_review_actions_request_created_at ON document_review_actions (tenant_id, document_request_id, created_at DESC);

CREATE TABLE IF NOT EXISTS ai_review_runs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  document_request_id UUID NOT NULL REFERENCES document_requests(id),
  submission_id UUID REFERENCES document_submissions(id),
  model_provider TEXT NOT NULL,
  model_name TEXT NOT NULL,
  verdict TEXT NOT NULL,
  feedback TEXT,
  trace_id TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_ai_review_runs_request_created_at ON ai_review_runs (tenant_id, document_request_id, created_at DESC);

CREATE TABLE IF NOT EXISTS ai_review_run_files (
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  ai_review_run_id UUID NOT NULL REFERENCES ai_review_runs(id),
  document_file_id UUID NOT NULL REFERENCES document_files(id),
  PRIMARY KEY (tenant_id, ai_review_run_id, document_file_id)
);

CREATE INDEX IF NOT EXISTS idx_ai_review_run_files_file ON ai_review_run_files (tenant_id, document_file_id);

CREATE TABLE IF NOT EXISTS external_contacts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  source TEXT NOT NULL,
  external_contact_id TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, source, external_contact_id)
);

CREATE TABLE IF NOT EXISTS user_external_contacts (
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  user_id UUID NOT NULL REFERENCES users(id),
  external_contact_id UUID NOT NULL REFERENCES external_contacts(id),
  linked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (tenant_id, user_id, external_contact_id)
);

CREATE TABLE IF NOT EXISTS conversations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  external_contact_id UUID REFERENCES external_contacts(id),
  channel_source TEXT NOT NULL,
  channel_id TEXT,
  lifecycle TEXT NOT NULL,
  assignee_principal_id UUID REFERENCES principals(id),
  ai_active BOOLEAN NOT NULL DEFAULT FALSE,
  is_handed_off BOOLEAN NOT NULL DEFAULT FALSE,
  is_waiting_for_legal_review BOOLEAN NOT NULL DEFAULT FALSE,
  opened_at TIMESTAMPTZ,
  closed_at TIMESTAMPTZ,
  meta JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_conversations_tenant_created_at ON conversations (tenant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversations_channel_lifecycle ON conversations (tenant_id, channel_source, lifecycle);
CREATE INDEX IF NOT EXISTS idx_conversations_assignee_lifecycle ON conversations (tenant_id, assignee_principal_id, lifecycle);
CREATE INDEX IF NOT EXISTS idx_conversations_external_contact ON conversations (tenant_id, external_contact_id);
CREATE INDEX IF NOT EXISTS idx_conversations_meta_gin ON conversations USING GIN (meta);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY,
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  actor_principal_id UUID REFERENCES principals(id),
  action TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id UUID,
  before JSONB,
  after JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE INDEX IF NOT EXISTS idx_audit_log_tenant_created_at ON audit_log (tenant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log (tenant_id, entity_type, entity_id, created_at DESC);

CREATE TABLE IF NOT EXISTS outbox_events (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY,
  tenant_id UUID NOT NULL REFERENCES tenants(id),
  event_type TEXT NOT NULL,
  payload JSONB NOT NULL,
  status TEXT NOT NULL DEFAULT 'pending',
  available_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_status_available_at ON outbox_events (status, available_at);
CREATE INDEX IF NOT EXISTS idx_outbox_tenant_created_at ON outbox_events (tenant_id, created_at DESC);

DO $$
DECLARE
  start_month DATE;
  next_month DATE;
  next_next_month DATE;
  p1_name TEXT;
  p2_name TEXT;
BEGIN
  start_month := date_trunc('month', now())::date;
  next_month := (start_month + INTERVAL '1 month')::date;
  next_next_month := (start_month + INTERVAL '2 month')::date;

  p1_name := 'audit_log_' || to_char(start_month, 'YYYY_MM');
  EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_log FOR VALUES FROM (%L) TO (%L)', p1_name, start_month, next_month);

  p2_name := 'audit_log_' || to_char(next_month, 'YYYY_MM');
  EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_log FOR VALUES FROM (%L) TO (%L)', p2_name, next_month, next_next_month);

  p1_name := 'outbox_events_' || to_char(start_month, 'YYYY_MM');
  EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF outbox_events FOR VALUES FROM (%L) TO (%L)', p1_name, start_month, next_month);

  p2_name := 'outbox_events_' || to_char(next_month, 'YYYY_MM');
  EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF outbox_events FOR VALUES FROM (%L) TO (%L)', p2_name, next_month, next_next_month);
END $$;
