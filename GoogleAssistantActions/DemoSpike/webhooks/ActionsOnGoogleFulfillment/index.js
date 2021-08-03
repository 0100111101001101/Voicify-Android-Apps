const { conversation } = require('@assistant/conversation');
const functions = require('firebase-functions');

const app = conversation({debug: true});
// creates a set object that contains words that require 'a' before referencing 
const optionsNeedA = new Set();
optionsNeedA.add('horse').add('phone')


// Event handler for greeting, this determines if the user has conversed with action  before and delivers a different response
app.handle('greeting', conv => {
 let message = 'A wondrous greeting, adventurer! Welcome back to the mythical land of Gryffinberg!';
 if (!conv.user.lastSeenTime) {
   message = 'Welcome to the mythical land of  Gryffinberg! Based on your clothes, you are not from around these lands. It looks like you\'re on your way to an epic journey.';
 }
 conv.add(message);
});

// Event handler for unavailable options, this determines when the letter 'a' needs to prepend the input to make better sense
app.handle('unavailable_options', conv => {
  // get user's raw input
  const option = conv.intent.params.chosenUnavailableOption.original;
  // match raw input to key (if a synonym was inputted) for logic processing
  const optionKey = conv.intent.params.chosenUnavailableOption.resolved;
  let message = 'I have seen the future and ';
  if(optionsNeedA.has(optionKey)){
    message = message + 'a ';
  }
  message = message + `${option} will not aid you on your journey. `;
  conv.add(message);
});

exports.ActionsOnGoogleFulfillment = functions.https.onRequest(app);