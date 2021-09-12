# This is a sample Python script.
# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

applications = [
    "Doordash",
    "UberEats",
    "Swiggy",
    "Zomato",
    'Slack',
    'Google',
    'COVIDSafe',
    'ZOOM',
    'Smart Launcher',
    'TikTok',
    'Houseparty',
    'Instagram',
    'Coronavirus Australia',
    'Disney',
    'WhatsApp Messenger',
    'YouTube',
    'Spotify',
    'Snapchat',
    'Facebook',
    'Doordash',
    'Netflix',
    'Gmail',
    'Google Maps',
    'Uber Eats',
    'mymacca’s Ordering & Offers',
    'Microsoft Teams',
    'Widgetsmith',
    'WikiCamps Australia',
    'The Wonder Weeks',
    'Sezzy Timer',
    'Procreate Pocket',
    'TripView - Sydney & Melbourne',
    'TouchRetouch',
    'Forest - Stay focused',
    'Monash University FODMAP diet',
    'AutoSleep Track Sleep on Watch',
    'Rain Parrot',
    'Facetune',
    'SkyView',
    'MusicView',
    'Shadowrocket',
    "Slack",
    'LastPass',
    'Grammarly',
    'Dropbox',
    'Google Drive',
    'OfficeSuite',
    'Imgur',
    'KineMaster',
    'Adobe Photoshop Express',
    'Adobe Lightroom',
    'Facebook Messenger',
    'Asana project management app',
    'Uber Eats',
    'Duolingo learn languages free',

]

actions = [
    'search',
    'find',
    'enter',
    'look up',
    'open',
    'get',
    'order',
    'launch',
    'click',
    'scroll',
    'swipe',
    'move'
]

foods = [
    'Fish and chips',
    'Sandwich',
    'Pita',
    'Hamburger',
    'Fried chicken',
    'French fries',
    'Onion ring',
    'Chicken nugget',
    'Taco',
    'Pizza',
    'Hot dog',
    'Ice cream',
    'Salad',
    'Marmalade',
    'Ham',
    'Egg',
    'Bread',
    'Breakfast burrito',
    'Hot chocolate',
    'Bacon',
    'Donut',
    'Porridge',
    'Muffin',
    'Waffle',
    'Tomato',
    'Croissant',
    'Pancake',
    'Toast',
    'sandwich',
    'Yogurt',
    'Cheese',
    'Milk',
    'Sausage',
    'Orange juice',
    'Breakfast cereal',
    'Coffee',
    'Fried chicken',
    'Omelet',
    'Pizza',
    'Kebab',
    'Fish',
    'Steak',
    'Broth',
    'Tossed salad',
    'Dressing',
    'Pasta',
    'Rice',
    'Soup'
]

fillers = [
    'Hey',
    'I',
    'Like',
    'You',
    'Ignore',
    'Can',
    'But',
    'About',
    'The',
    'So',
    'Please',
    'We',
    'a',
    'an'
]

item = [
    'iPhone',
    'Hot toys',
    'comics',
    'carpets',
    'crockery'
]


def data_create():

    outputStr = []
    index = 0
    for word1 in applications:
        for word2 in actions:
            for word3 in foods:
                string = word2 + " " + word3 + " from " + word1

                if index//22 == 0:
                    string += string + "s"

                elif index//24 == 0:
                    string += string + "es"

                if index//27 == 0:
                    string = word2 + " " + word3 + " from the " + word1

                elif index//26 == 0:
                    string = word2 + " " + word3 + " from a " + word1

                index += 1

                outputStr.append(string)

    for word1 in applications:
        for word2 in actions:
            for word4 in item:
                string = word2 + " " + word1 + ' and then ' + word2 + ' down and search ' + word4

                outputStr.append(string)

    print(outputStr)
    return outputStr


def entity_analysis(content, doc):

    content = content.strip()
    data = clean_data(content)
    finalOutput = '{"annotations":['
    index = 0

    if len(data) != 0:
        for word in data:

            output1 = '{"text_extraction":{"text_segment":{"end_offset":' + str(word[2]) + \
                      ',"start_offset":' + str(word[1]) + '}},"display_name":' + '"' + word[0] + '"' + '}'

            if index != 0:
                finalOutput = finalOutput + ',' + output1
            else:
                finalOutput += output1
            index += 1

        output3 = '],"text_snippet":{"content":' + '"' + content + '"' + '}}\n'
        finalOutput += output3

    else:
        print("No content!")

    value = str(finalOutput)
    doc.write(str(value))


def clean_data(unclean_data):

    # unclean_data = joinApp(unclean_data)
    unclean_data = unclean_data.split(' ')
    end_offset = 0
    data = []

    for word in unclean_data:

        entity = ''

        if end_offset == 0:
            start_offset = 0
        else:
            start_offset = end_offset + 2
        end_offset = start_offset + len(word)

        word = word.lower()
        check = False
        for word0 in fillers:
            word0 = word0.lower()
            if word == word0:
                check = True
                break

        if check:
            continue

        check1 = False
        for word1 in applications:
            word1 = word1.lower()
            if word == word1:
                entity = "Applications"
                check1 = True

        check2 = False
        if entity == '':
            for word2 in foods:
                word2 = word2.lower()
                if word == word2:
                    entity = "Foods"
                    check2 = True

        check3 = False
        if entity == '':
            for word3 in actions:
                word3 = word3.lower()
                if word == word3:
                    entity = "Actions"
                    check3 = True

        # check4 = False
        # if entity == '':
        #     for word4 in food_application:
        #         word4 = word4.lower()
        #         if word == word4:
        #             entity = "Food Application"
        #             check4 = True

        if not (check1 or check2 or check3):
            continue

        data.append((entity, start_offset, end_offset))

    return data


# def joinApp(content):
#
#     end_offset = 0
#     mark = 0
#
#     content_list = content.split(' ')
#     for word in content_list:
#         word = word.lower()
#
#         if end_offset == 0:
#             start_offset = 0
#         else:
#             start_offset = end_offset + 2
#         end_offset = start_offset + len(word) - 1
#
#         for word3 in actions:
#             word3 = word3.lower()
#             if word == word3:
#                 mark = end_offset + 1
#                 break
#
#     return content


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    dataset = data_create()
    # test = "Hey I like android development so can you please launch UberEATS we google about the proposal"
    file = open('label_data.jsonl', 'w', encoding='utf-8')
    for commands in dataset:
        entity_analysis(commands, file)
    file.close()
