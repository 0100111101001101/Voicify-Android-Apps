# Actions on Google: Conversational Actions 
### Prerequisites
1. Node.js and NPM
    + We recommend installing using [nvm for Linux/Mac](https://github.com/creationix/nvm) and [nvm-windows for Windows](https://github.com/coreybutler/nvm-windows)
1. Install the [Firebase CLI](https://developers.google.com/assistant/actions/dialogflow/deploy-fulfillment)
    + We recommend using MAJOR version `8` , `npm install -g firebase-tools@^8.0.0`
    + Run `firebase login` with your Google account
3. Projects will need to be associated with a billing account, although you won't be charged under the free tier. Follow terminal messages to activate reequired APIs in project
### Setup
#### Actions Console
1. From the [Actions on Google Console](https://console.actions.google.com/), **New project** > **Create project** > under **What kind of Action do you want to build?** > **Custom** > **Blank project**

#### Actions CLI
1. Read [Guide](https://developers.google.com/assistant/conversational/quickstart)
1. Install the [Actions CLI](https://developers.google.com/assistant/actionssdk/gactions).
1. Navigate to `PROJECT_NAME/settings/settings.yaml`, and replace `<PROJECT_ID>` with your project ID.
1. Run `gactions login` to login to your account.
1. Run `gactions push` to push your project.
1. Run `gactions deploy preview` to deploy your project.

### Running this Sample
+ You can test your Action on any Google Assistant-enabled device on which the Assistant is signed into the same account used to create this project. Just say or type, “OK Google, talk to my test app”. or “OK Google, talk to my **displayname**”
+ You can also use the Actions on Google Console simulator to test most features and preview on-device behavior.

## References & Issues
+ Questions? Go to [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google) or the [Assistant Developer Community on Reddit](https://www.reddit.com/r/GoogleAssistantDev/).
+ Actions SDK and Builder Quick Start Guide [Documentation](https://developers.google.com/assistant/conversational/quickstart)
+ Actions on Google [Documentation](https://developers.google.com/assistant)
+ Actions on Google [Codelabs](https://codelabs.developers.google.com/?cat=Assistant) - Very useful for learning
