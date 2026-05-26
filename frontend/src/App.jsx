import { useEffect, useMemo, useState } from "react";

const SECTIONS = [
  "Overview",
  "Auth",
  "Uploads",
  "Files",
  "Snapshots",
  "Integrity",
  "Admin"
];

const defaultApiBase = "http://localhost:8080";

const statusStyles = {
  ok: "status-good",
  fail: "status-bad"
};

export default function App() {
  const [section, setSection] = useState("Overview");
  const [apiBase, setApiBase] = useState(
    localStorage.getItem("vf_api_base") || defaultApiBase
  );
  const [token, setToken] = useState(localStorage.getItem("vf_token") || "");
  const [refreshToken, setRefreshToken] = useState(localStorage.getItem("vf_refresh_token") || "");
  const [lastResponse, setLastResponse] = useState("Ready");

  useEffect(() => {
    localStorage.setItem("vf_api_base", apiBase);
  }, [apiBase]);

  useEffect(() => {
    localStorage.setItem("vf_token", token);
  }, [token]);

  useEffect(() => {
    localStorage.setItem("vf_refresh_token", refreshToken);
  }, [refreshToken]);

  const api = useMemo(() => {
    return async (path, options = {}) => {
      const request = async (accessToken) => {
        const headers = new Headers(options.headers || {});
        headers.set("Accept", "application/json");
        if (!(options.body instanceof FormData)) {
          headers.set("Content-Type", "application/json");
        }
        if (accessToken) {
          headers.set("Authorization", `Bearer ${accessToken}`);
        }
        return fetch(`${apiBase}${path}`, {
          ...options,
          headers
        });
      };

      let response = await request(token);
      if (response.status === 401 && refreshToken && path !== "/api/v1/auth/refresh") {
        const refreshResponse = await fetch(`${apiBase}/api/v1/auth/refresh`, {
          method: "POST",
          headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
          },
          body: JSON.stringify({ refreshToken })
        });
        const refreshText = await refreshResponse.text();
        const refreshPayload = refreshText ? safeJson(refreshText) : {};
        if (refreshResponse.ok && refreshPayload.accessToken) {
          setToken(refreshPayload.accessToken);
          setRefreshToken(refreshPayload.refreshToken || refreshToken);
          response = await request(refreshPayload.accessToken);
        }
      }

      const text = await response.text();
      const payload = text ? safeJson(text) : {};
      if (!response.ok) {
        throw new Error(formatApiError(response, payload));
      }
      return payload;
    };
  }, [apiBase, token, refreshToken]);

  const logout = () => {
    setToken("");
    setRefreshToken("");
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">SafeStore</div>
        <div className="nav">
          {SECTIONS.map((item) => (
            <button
              key={item}
              className={item === section ? "active" : ""}
              onClick={() => setSection(item)}
            >
              {item}
            </button>
          ))}
        </div>
      </aside>
      <main className="main">
        <header className="header">
          <div>
            <h1>{section}</h1>
            <div className="subtitle">Secure backup command plane</div>
          </div>
          <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
            <span className={token ? "auth-badge signed-in" : "auth-badge"}>
              {token ? "Signed in" : "No token"}
            </span>
            <input
              value={apiBase}
              onChange={(event) => setApiBase(event.target.value)}
              placeholder="API Base"
            />
            <button className="secondary" onClick={logout}>Logout</button>
          </div>
        </header>

        {section === "Overview" && (
          <OverviewPanel lastResponse={lastResponse} />
        )}
        {section === "Auth" && (
          <AuthPanel
            api={api}
            setToken={setToken}
            setRefreshToken={setRefreshToken}
            setLastResponse={setLastResponse}
          />
        )}
        {section === "Uploads" && (
          <UploadPanel api={api} token={token} setLastResponse={setLastResponse} />
        )}
        {section === "Files" && (
          <FilesPanel api={api} setLastResponse={setLastResponse} />
        )}
        {section === "Snapshots" && (
          <SnapshotPanel api={api} setLastResponse={setLastResponse} />
        )}
        {section === "Integrity" && (
          <IntegrityPanel api={api} setLastResponse={setLastResponse} />
        )}
        {section === "Admin" && (
          <AdminPanel api={api} setLastResponse={setLastResponse} />
        )}
        {section !== "Overview" && (
          <ResponsePanel lastResponse={lastResponse} />
        )}
      </main>
    </div>
  );
}

function OverviewPanel({ lastResponse }) {
  return (
    <div className="card-grid">
      <div className="card">
        <h3>System Signal</h3>
        <div className="kpis">
          <div className="kpi">
            <div className="label">API</div>
            <div className="value">Online</div>
          </div>
          <div className="kpi">
            <div className="label">Workers</div>
            <div className="value">Active</div>
          </div>
          <div className="kpi">
            <div className="label">Dedup Rate</div>
            <div className="value">72%</div>
          </div>
        </div>
        <div className="chart">
          <div className="chart-bar" style={{ "--fill": "70%" }}><span>Uploads</span></div>
          <div className="chart-bar" style={{ "--fill": "45%" }}><span>Restore</span></div>
          <div className="chart-bar" style={{ "--fill": "80%" }}><span>Chunks</span></div>
          <div className="chart-bar" style={{ "--fill": "55%" }}><span>Cache</span></div>
        </div>
      </div>
      <div className="card">
        <h3>Last Response</h3>
        <div className="mono">{lastResponse}</div>
      </div>
    </div>
  );
}

function ResponsePanel({ lastResponse }) {
  return (
    <div className="card response-card">
      <h3>Last Response</h3>
      <div className="mono">{lastResponse}</div>
    </div>
  );
}

function AuthPanel({ api, setToken, setRefreshToken, setLastResponse }) {
  const [signup, setSignup] = useState({ name: "", email: "", password: "" });
  const [login, setLogin] = useState({ email: "", password: "" });
  const [manualRefreshToken, setManualRefreshToken] = useState("");

  const run = async (fn) => {
    try {
      const result = await fn();
      setLastResponse(JSON.stringify(result, null, 2));
      if (result.accessToken) {
        setToken(result.accessToken);
      }
      if (result.refreshToken) {
        setRefreshToken(result.refreshToken);
      }
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  return (
    <div className="card-grid">
      <div className="card">
        <h3>Signup</h3>
        <div className="form-grid">
          <input placeholder="Name" value={signup.name} onChange={(e) => setSignup({ ...signup, name: e.target.value })} />
          <input placeholder="Email" value={signup.email} onChange={(e) => setSignup({ ...signup, email: e.target.value })} />
          <input placeholder="Password" type="password" value={signup.password} onChange={(e) => setSignup({ ...signup, password: e.target.value })} />
        </div>
        <button className="primary" onClick={() => run(() => api("/api/v1/auth/signup", { method: "POST", body: JSON.stringify(signup) }))}>
          Create User
        </button>
      </div>
      <div className="card">
        <h3>Login</h3>
        <div className="form-grid">
          <input placeholder="Email" value={login.email} onChange={(e) => setLogin({ ...login, email: e.target.value })} />
          <input placeholder="Password" type="password" value={login.password} onChange={(e) => setLogin({ ...login, password: e.target.value })} />
        </div>
        <button className="primary" onClick={() => run(() => api("/api/v1/auth/login", { method: "POST", body: JSON.stringify(login) }))}>
          Login
        </button>
      </div>
      <div className="card">
        <h3>Refresh Token</h3>
        <div className="form-grid">
          <input
            placeholder="Refresh Token"
            value={manualRefreshToken}
            onChange={(e) => setManualRefreshToken(e.target.value)}
          />
        </div>
        <button className="primary" onClick={() => run(() => api("/api/v1/auth/refresh", { method: "POST", body: JSON.stringify({ refreshToken: manualRefreshToken }) }))}>
          Refresh
        </button>
      </div>
    </div>
  );
}

function UploadPanel({ api, token, setLastResponse }) {
  const [file, setFile] = useState(null);
  const [tags, setTags] = useState("");

  const uploadMultipart = async () => {
    if (!token) {
      setLastResponse("Log in first, then try the upload again.");
      return;
    }
    if (!file) {
      setLastResponse("Select a file first");
      return;
    }
    const formData = new FormData();
    formData.append("file", file);
    if (tags) {
      formData.append("tags", tags);
    }
    try {
      setLastResponse("Uploading...");
      const response = await api("/api/v1/files/upload/multipart", {
        method: "POST",
        body: formData
      });
      setLastResponse(JSON.stringify(response, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  return (
    <div className="card">
      <h3>Multipart Upload</h3>
      <div className="form-grid">
        <input type="file" onChange={(e) => setFile(e.target.files?.[0] || null)} />
        <input placeholder="Tags" value={tags} onChange={(e) => setTags(e.target.value)} />
      </div>
      <button className="primary" onClick={uploadMultipart}>Upload File</button>
    </div>
  );
}

function FilesPanel({ api, setLastResponse }) {
  const [fileId, setFileId] = useState("");
  const [search, setSearch] = useState({ filename: "", ownerId: "", tags: "" });
  const [results, setResults] = useState([]);

  const runSearch = async () => {
    try {
      const data = await api("/api/v1/metadata/search", {
        method: "POST",
        body: JSON.stringify(search)
      });
      setResults(data.content || []);
      setLastResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  const deleteFile = async () => {
    try {
      await api(`/api/v1/files/${fileId}`, { method: "DELETE" });
      setLastResponse("Deleted");
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  const loadMetadata = async () => {
    try {
      const data = await api(`/api/v1/files/${fileId}/metadata`);
      setLastResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  return (
    <div className="card-grid">
      <div className="card">
        <h3>Metadata Search</h3>
        <div className="form-grid">
          <input placeholder="Filename" value={search.filename} onChange={(e) => setSearch({ ...search, filename: e.target.value })} />
          <input placeholder="Owner ID" value={search.ownerId} onChange={(e) => setSearch({ ...search, ownerId: e.target.value })} />
          <input placeholder="Tags" value={search.tags} onChange={(e) => setSearch({ ...search, tags: e.target.value })} />
        </div>
        <button className="primary" onClick={runSearch}>Search</button>
        <table className="table">
          <thead>
            <tr>
              <th>File</th>
              <th>Owner</th>
              <th>Size</th>
              <th>Checksum</th>
            </tr>
          </thead>
          <tbody>
            {results.map((row) => (
              <tr key={row.id}>
                <td>{row.filename}</td>
                <td className="mono">{row.ownerId}</td>
                <td>{row.totalSize}</td>
                <td className="mono">{row.checksum}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="card">
        <h3>File Actions</h3>
        <div className="form-grid">
          <input placeholder="File ID" value={fileId} onChange={(e) => setFileId(e.target.value)} />
        </div>
        <div style={{ display: "flex", gap: 10, marginTop: 12 }}>
          <button className="secondary" onClick={loadMetadata}>Get Metadata</button>
          <button className="secondary" onClick={() => window.open(`${apiBaseForDownload(fileId)}`, "_blank")}>Download</button>
          <button className="primary" onClick={deleteFile}>Delete</button>
        </div>
      </div>
    </div>
  );
}

function SnapshotPanel({ api, setLastResponse }) {
  const [fileId, setFileId] = useState("");
  const [versionId, setVersionId] = useState("");
  const [compare, setCompare] = useState({ a: "", b: "" });

  const createSnapshot = async () => {
    try {
      await api("/api/v1/snapshots/create", {
        method: "POST",
        body: JSON.stringify({ fileId })
      });
      setLastResponse("Snapshot created");
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  const restoreSnapshot = async () => {
    try {
      const response = await api("/api/v1/snapshots/restore", {
        method: "POST",
        body: JSON.stringify({ fileVersionId: versionId })
      });
      setLastResponse(JSON.stringify(response, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  const listVersions = async () => {
    try {
      const response = await api(`/api/v1/snapshots/files/${fileId}`);
      setLastResponse(JSON.stringify(response, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  const compareVersions = async () => {
    try {
      const response = await api(`/api/v1/snapshots/compare?versionA=${compare.a}&versionB=${compare.b}`);
      setLastResponse(JSON.stringify(response, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  return (
    <div className="card-grid">
      <div className="card">
        <h3>Snapshots</h3>
        <div className="form-grid">
          <input placeholder="File ID" value={fileId} onChange={(e) => setFileId(e.target.value)} />
        </div>
        <div style={{ display: "flex", gap: 10, marginTop: 12 }}>
          <button className="primary" onClick={createSnapshot}>Create Snapshot</button>
          <button className="secondary" onClick={listVersions}>List Versions</button>
        </div>
      </div>
      <div className="card">
        <h3>Restore</h3>
        <div className="form-grid">
          <input placeholder="File Version ID" value={versionId} onChange={(e) => setVersionId(e.target.value)} />
        </div>
        <button className="primary" onClick={restoreSnapshot}>Restore</button>
      </div>
      <div className="card">
        <h3>Compare</h3>
        <div className="form-grid">
          <input placeholder="Version A" value={compare.a} onChange={(e) => setCompare({ ...compare, a: e.target.value })} />
          <input placeholder="Version B" value={compare.b} onChange={(e) => setCompare({ ...compare, b: e.target.value })} />
        </div>
        <button className="secondary" onClick={compareVersions}>Compare</button>
      </div>
    </div>
  );
}

function IntegrityPanel({ api, setLastResponse }) {
  const [fileId, setFileId] = useState("");
  const [checksum, setChecksum] = useState("");

  const validate = async () => {
    try {
      const data = await api(`/api/v1/integrity/${fileId}`);
      setLastResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  const report = async () => {
    try {
      const data = await api(`/api/v1/integrity/report/${fileId}`);
      setLastResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  const verify = async () => {
    try {
      const data = await api("/api/v1/integrity/verify", {
        method: "POST",
        body: JSON.stringify({ fileId, expectedChecksum: checksum })
      });
      setLastResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  return (
    <div className="card">
      <h3>Integrity Checks</h3>
      <div className="form-grid">
        <input placeholder="File ID" value={fileId} onChange={(e) => setFileId(e.target.value)} />
        <input placeholder="Expected Checksum" value={checksum} onChange={(e) => setChecksum(e.target.value)} />
      </div>
      <div style={{ display: "flex", gap: 10, marginTop: 12 }}>
        <button className="secondary" onClick={validate}>Validate</button>
        <button className="secondary" onClick={report}>Report</button>
        <button className="primary" onClick={verify}>Verify</button>
      </div>
    </div>
  );
}

function AdminPanel({ api, setLastResponse }) {
  const [metrics, setMetrics] = useState(null);
  const [auditFilters, setAuditFilters] = useState({ userId: "", action: "", entityType: "" });
  const [auditRows, setAuditRows] = useState([]);

  const loadMetrics = async () => {
    try {
      const data = await api("/api/v1/admin/metrics/uploads");
      setMetrics(data);
      setLastResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  const searchAudit = async () => {
    try {
      const data = await api("/api/v1/admin/audit/search", {
        method: "POST",
        body: JSON.stringify(auditFilters)
      });
      setAuditRows(data.content || []);
      setLastResponse(JSON.stringify(data, null, 2));
    } catch (err) {
      setLastResponse(err.message);
    }
  };

  return (
    <div className="card-grid">
      <div className="card">
        <h3>Upload Metrics</h3>
        <button className="primary" onClick={loadMetrics}>Refresh Metrics</button>
        {metrics && (
          <div className="kpis" style={{ marginTop: 12 }}>
            <div className="kpi"><div className="label">Pending</div><div className="value">{metrics.pending}</div></div>
            <div className="kpi"><div className="label">Processing</div><div className="value">{metrics.processing}</div></div>
            <div className="kpi"><div className="label">Completed</div><div className="value">{metrics.completed}</div></div>
            <div className="kpi"><div className="label">Failed</div><div className="value">{metrics.failed}</div></div>
          </div>
        )}
      </div>
      <div className="card">
        <h3>Audit Search</h3>
        <div className="form-grid">
          <input placeholder="User ID" value={auditFilters.userId} onChange={(e) => setAuditFilters({ ...auditFilters, userId: e.target.value })} />
          <input placeholder="Action" value={auditFilters.action} onChange={(e) => setAuditFilters({ ...auditFilters, action: e.target.value })} />
          <input placeholder="Entity Type" value={auditFilters.entityType} onChange={(e) => setAuditFilters({ ...auditFilters, entityType: e.target.value })} />
        </div>
        <button className="secondary" onClick={searchAudit}>Search</button>
        <table className="table">
          <thead>
            <tr>
              <th>Time</th>
              <th>Action</th>
              <th>Entity</th>
              <th>User</th>
            </tr>
          </thead>
          <tbody>
            {auditRows.map((row) => (
              <tr key={row.id}>
                <td>{row.timestamp}</td>
                <td>{row.action}</td>
                <td>{row.entityType}</td>
                <td className="mono">{row.userId}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function safeJson(text) {
  try {
    return JSON.parse(text);
  } catch (err) {
    return text;
  }
}

function formatApiError(response, payload) {
  if (payload && typeof payload === "object") {
    return payload.error || payload.message || `${response.status} ${response.statusText || "Request failed"}`;
  }
  if (typeof payload === "string" && payload.trim()) {
    return payload;
  }
  if (response.status === 401) {
    return "401 Unauthorized. Log in first, then try the upload again.";
  }
  if (response.status === 403) {
    return "403 Forbidden. Your account does not have permission for this action.";
  }
  return `${response.status} ${response.statusText || "Request failed"}`;
}

function apiBaseForDownload(fileId) {
  const base = localStorage.getItem("vf_api_base") || defaultApiBase;
  return `${base}/api/v1/files/${fileId}/download`;
}
