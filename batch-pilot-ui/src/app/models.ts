export interface JobParameter {
  name: string;
  value: string | null;
  type: string | null;
  identifying: boolean;
}

export interface ExecutionSummary {
  executionId: number;
  instanceId: number | null;
  jobName: string | null;
  status: string | null;
  exitCode: string | null;
  createTime: string | null;
  startTime: string | null;
  endTime: string | null;
  durationMs: number | null;
  parameters: JobParameter[];
}

export interface JobSummary {
  name: string;
  instanceCount: number;
  lastExecution: ExecutionSummary | null;
}

export interface StepExecution {
  id: number;
  stepName: string;
  status: string | null;
  readCount: number;
  writeCount: number;
  commitCount: number;
  rollbackCount: number;
  readSkipCount: number;
  writeSkipCount: number;
  processSkipCount: number;
  filterCount: number;
  exitCode: string | null;
  exitDescription: string | null;
  startTime: string | null;
  endTime: string | null;
  durationMs: number | null;
  failureExceptions: string[];
}

export interface ActionsCapabilities {
  restart: boolean;
  stop: boolean;
  launch: boolean;
}

export type ParamType = 'STRING' | 'LONG' | 'DOUBLE' | 'DATE';

export interface LaunchParamInput {
  name: string;
  value: string;
  type: ParamType;
  identifying: boolean;
}

export interface AuditEvent {
  id: string;
  eventTime: string | null;
  principal: string | null;
  action: string;
  target: string | null;
  result: string;
  message: string | null;
}

export interface ExecutionDetail {
  executionId: number;
  instanceId: number | null;
  jobName: string | null;
  status: string | null;
  exitCode: string | null;
  exitDescription: string | null;
  createTime: string | null;
  startTime: string | null;
  endTime: string | null;
  durationMs: number | null;
  parameters: JobParameter[];
  steps: StepExecution[];
  failureExceptions: string[];
}
