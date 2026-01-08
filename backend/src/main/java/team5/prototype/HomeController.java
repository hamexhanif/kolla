package team5.prototype;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Kolla Backend API</title>
                <style>
                    body { font-family: Arial, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }
                    h1 { color: #333; }
                    .endpoint { background: #f5f5f5; padding: 10px; margin: 10px 0; border-left: 4px solid #007bff; }
                    .method { font-weight: bold; color: #007bff; }
                    code { background: #e9ecef; padding: 2px 6px; border-radius: 3px; }
                </style>
            </head>
            <body>
                <h1>🚀 Kolla Backend API</h1>
                <p>Welcome to the Kolla Backend API. This is a REST API for workflow management.</p>
                
                <h2>Quick Start</h2>
                <div class="endpoint">
                    <span class="method">POST</span> <code>/api/auth/login</code><br>
                    Login to get a JWT token<br>
                    Body: <code>{"email": "admin@kolla.com", "password": "adminpassword"}</code>
                </div>
                
                <h2>Available Endpoints</h2>
                
                <h3>Authentication</h3>
                <div class="endpoint">
                    <span class="method">POST</span> <code>/api/auth/login</code> - Login and get JWT token
                </div>
                
                <h3>Tasks</h3>
                <div class="endpoint">
                    <span class="method">GET</span> <code>/api/tasks</code> - Get all tasks (requires auth)<br>
                    <span class="method">POST</span> <code>/api/tasks</code> - Create a new task (requires auth)<br>
                    <span class="method">GET</span> <code>/api/tasks/{id}</code> - Get task by ID (requires auth)
                </div>
                
                <h3>Manager Dashboard</h3>
                <div class="endpoint">
                    <span class="method">GET</span> <code>/api/manager/dashboard</code> - Get manager dashboard (requires WORKFLOW_MANAGER role)
                </div>
                
                <h3>Database Console</h3>
                <div class="endpoint">
                    <span class="method">GET</span> <code>/h2-console</code> - H2 Database Console (no auth required)
                </div>
                
                <h2>Test Users</h2>
                <ul>
                    <li><strong>Admin/Manager:</strong> admin@kolla.com / adminpassword</li>
                    <li><strong>Developer:</strong> dev@kolla.com / devpassword</li>
                    <li><strong>Tester:</strong> tester@kolla.com / testpassword</li>
                </ul>
                
                <h2>API Documentation</h2>
                <p>Check the <code>api-requests.http</code> file in the project for more examples.</p>
                
                <p><em>API is running on port 8080</em></p>
            </body>
            </html>
            """;
    }
}

