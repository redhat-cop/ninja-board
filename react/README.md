This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

# Running

1.  `npm install`
2.  `npm start`

# Current Status

The user management features are the big ones to tackle now. Google OIDC is implemented, but the "implicit" flow is implemented and you should implement the "server" flow.

The /registration-page is for submitting a request from the user to the backend hosted on Quarkus, which creates a user in the Giveback Ninja system. It was going to be repurposed into the "Edit Profile" page, with the first time logging in through Google sending the necessary info to "register" (create) a user. From the "Edit Profile" page, a user could then update more information as needed.

# Background

This project is set up with React.js, leveraging components from [PatternFly](https://www.patternfly.org). PatternFly is a library of reusable, familiar components on any webpage, e.g. Navigation Bar, Table, Buttons, Forms, etc. It includes CSS, so you don't have to worry about styling if you don't want to. It is recommended to use their CSS, as it is inline with the branding of PatternFly and Red Hat.

Any files you need to use not related to JavaScript should be stored in [assets](src/assets). This includes CSS files, images, and other media.

A majority of development work will happen in [components](src/components). These are the building blocks of your application, and good component design is crucial to a maintainable codebase. Since PatternFly is being used, custom components need to roll up into PatternFly wrappers, like `PageSection`, `PageHeader`, and `Page`. See PatternFly documentation for more info.

Any configuration data should be stored in [config](src/config) and imported in the appropriate JS file.

The top level of this project, with files such as `App.js`, `AppLayout.jsx`, and `AppRoutes.jsx` should stay as lean as possible, and be only for files that affect the entire project. If you cannot prefix your file name with `App` and it not make sense, then it most likely does not belong here.

## PatternFly Tips

-   Any component you want to use as a page in the application needs to be wrapped in `<PageSection></PageSection>`
-   If you need to access the state of a PatternFly component, use a [React ref](https://reactjs.org/docs/refs-and-the-dom.html).
-   Use the documentation to see what properties a component takes.

# Known issues and potential improvements

1.  When page shrinks, NavBar pops out of Header and has different styling.
2.  Start up performance sucks

# create-react-app Generated README

Below is information automatically set up by [Create React App](https://github.com/facebook/create-react-app).

## Available Scripts

In the project directory, you can run:

### `yarn start`

Runs the app in the development mode.<br />
Open <http://localhost:3000> to view it in the browser.

The page will reload if you make edits.<br />
You will also see any lint errors in the console.

### `yarn test`

Launches the test runner in the interactive watch mode.<br />
See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

### `yarn build`

Builds the app for production to the `build` folder.<br />
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.<br />
Your app is ready to be deployed!

See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.

### `yarn eject`

**Note: this is a one-way operation. Once you `eject`, you can’t go back!**

If you aren’t satisfied with the build tool and configuration choices, you can `eject` at any time. This command will remove the single build dependency from your project.

Instead, it will copy all the configuration files and the transitive dependencies (webpack, Babel, ESLint, etc) right into your project so you have full control over them. All of the commands except `eject` will still work, but they will point to the copied scripts so you can tweak them. At this point you’re on your own.

You don’t have to ever use `eject`. The curated feature set is suitable for small and middle deployments, and you shouldn’t feel obligated to use this feature. However we understand that this tool wouldn’t be useful if you couldn’t customize it when you are ready for it.

## Learn More

You can learn more in the [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).

To learn React, check out the [React documentation](https://reactjs.org/).

### Code Splitting

This section has moved here: <https://facebook.github.io/create-react-app/docs/code-splitting>

### Analyzing the Bundle Size

This section has moved here: <https://facebook.github.io/create-react-app/docs/analyzing-the-bundle-size>

### Making a Progressive Web App

This section has moved here: <https://facebook.github.io/create-react-app/docs/making-a-progressive-web-app>

### Advanced Configuration

This section has moved here: <https://facebook.github.io/create-react-app/docs/advanced-configuration>

### Deployment

This section has moved here: <https://facebook.github.io/create-react-app/docs/deployment>

### `yarn build` fails to minify

This section has moved here: <https://facebook.github.io/create-react-app/docs/troubleshooting#npm-run-build-fails-to-minify>
