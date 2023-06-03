# AppTracky

Job application tracking demo app made mostly to:
- Familiarise with OAuth authentication flows on Spring Boot.
- AWS practice (ECS, ECR, IAM, S3, S3 presigning, RDS, SSM Parameter store)
- Try my hands at browser drag-drop APIs

Done:
- [x] Authentication flows (APIs / frontend)
   - [x] Basic auth (login, registrartion, forgot password, change password, email validation)
   - [x] Oauth/OIDC (Google login/registration)
   - [x] Federated identity (associating Google account to validated basic auth accounts)
- [x] Frontend
   - [x] Layout scaffolding
   - [x] Authentication related things
- [x] Drag-and-drop kanban board for applications and APIs
- [x] Presigned urls S3 uploads for PDF resumes
- [x] AWS SSM Parameter Store integration for OAuth/RDS secrets
- [x] Containerize the backend for easier development, and ECS deployment
- [x] Setup ECS task defs, cluster, S3 bucket policies, IAM for GH actions
- [x] Build and deploy image to ECS, and frontend to S3 automatically with GH actions

TBD:
- [ ] ($$) (Maybe) HTTPs and domains for S3 frontend and ALB to front ECS (instead of dynamic public IP)
- [ ] (Maybe) CloudFormation-ify everything AWS related

The backend is built with Spring Boot, and heavy use of Spring Security for Authentication.

Both basic auth and oauth flows ultimately result in the issuing of a JWT used for endpoint authentication and authorization. 

Frontend is a simple Vue app built on Vuetify + Vite.


## URLS

S3 Frontend: http://zyang-apptracky.s3-website-ap-northeast-1.amazonaws.com/

ECS Backend: (dynamic IP) (currently taken offline to save $$)

## Screenshots

![](./screenshots/ss6.png)

![](./screenshots/ss5.png)

![](./screenshots/ss1.png)

![](./screenshots/ss2.png)

![](./screenshots/ss3.png)

![](./screenshots/ss4.png)
