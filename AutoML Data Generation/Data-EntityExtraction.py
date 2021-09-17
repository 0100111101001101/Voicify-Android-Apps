# This is a sample Python script.
# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.


normal_app_names = [
    "UberEats",
    "Swiggy",
    "Zomato",
    'Google',
    'TikTok',
    'Houseparty',
    'Instagram',
    'Disney',
    'YouTube',
    'Snapchat',
    'Facebook',
    'Doordash',
    'Netflix',
    'Gmail',
    'SkyView',
    'MusicView',
    'Shadowrocket',
    "OpenTable",
    'Grammarly',
    'Dropbox',
    'Foodora',
    'Menulog',
    'Deliveroo',
    'EatNow',
    'Waze',
    'Mapquest',

]

faulty_app_names = [
    'musics player',
    'musical player',
    'store',
    'google eats',
    'Gogle Maps',
    'uber dash',
    'MenuEats',
    'Wave',
    'Delivery'
    'UberEat'

]

unnecessary_word_app_names = [
    'Facebook Messenger',
    'Asana project management app',
    'Google Play Store',
    'Google Maps',
    'Google Gmail',
    'Uber Eats order food',
    'Doordash delivery',


]

multi_word_apps = [
    'Mapfactor Navigator'
    'Scout GPS',
    'Happy Cow',
    'Restaurant Finder',
    'WhatsApp Messenger',
    'WikiCamps Australia',
    'The Wonder Weeks',
    'Sezzy Timer',
    'Adobe Photoshop Express',
    'Adobe Lightroom',
    'mymacca’s Ordering & Offers',
    'Microsoft Teams',
    'TripView - Sydney & Melbourne',
    'AutoSleep Track Sleep on Watch',
]

click_actions = [
    'click', 'tap', 'touch'
]

open_actions = [
    'open', 'launch', 'start'
]

scroll_actions = [
    'scroll up',
    'swipe up',
    'scroll down',
    'swipe down',
    'scroll left',
    'swipe left',
    'scroll right',
    'swipe right',
]

enter_actions = [
    'enter', 'search', 'find', 'look up',
]

plurals = [
    "buses", "boxes", "boats", "women", "children", "cinemas", "hoodies"
]
locations = [
    'Fish and chips',
    'Hamburger',
    'Pizza',
    'Hot dog',
    'Croissant',
    'sandwich',
    'Coffee',
    'Fried chicken',
    'Pizza',
    'Kebab',
    'bar',
    'bank',
    'bakery',
    'airport',
    'bookstore ',
    'station',
    'church',
    'court',
    'department store',
    'cinema ',
    'gym',
    'hospital',
    'hotel',
    'zoo',
    'supermarket',
    'school',
    'restaurant',
    'park',
    'motel',
    'pharmacy',
    'museum',
    'mall',
    'library',
    'library',
    'motel',
    'ATM',
    'kindergarten',
    'Fast food'
]

all_apps = [*normal_app_names, *multi_word_apps, *faulty_app_names, *unnecessary_word_app_names]

articles = [
    'a',
    'an',
    'the'
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

]

items = [
    'clothes',
    'grocery',
    'food',
    'shoes',
    'cosmetics',
    'equipment',
    'electronics',
    'household appliance',
    'smartphone',
    'juice'
]

conjunctions = [
    "or", "and", "not", "then", "after that"
]


def data_create():
    outputStr = []
    index = 0
    for app in normal_app_names:
        for word2 in open_actions:
            for word3 in items:
                string = word2 + " " + word3 + " from " + app
                if index // 22 == 0:
                    string += string + "s"

                elif index // 24 == 0:
                    string += string + "es"

                if index // 27 == 0:
                    string = word2 + " " + word3 + " from the " + app

                elif index // 26 == 0:
                    string = word2 + " " + word3 + " from a " + app
                index += 1
                # outputStr.append(string)
        for word2 in open_actions:
            for word4 in items:
                string = word2 + " " + app + ' and then ' + word2 + ' down and search ' + word4
                # outputStr.append(string)

    # open (App name) app
    counter = 0
    for open_word in open_actions:
        for app in all_apps:
            counter = counter + 1
            if counter % 8 == 0:
                continue
            if counter % 5 == 0:
                string = open_word + " the " + app
            elif counter % 7 == 0:
                string = open_word + " a " + app
            else:
                string = open_word + " " + app
            if counter % 20 == 0:
                string += " app"

            outputStr.append(string)

    # search something in app
    counter = 0
    swipe_counter = 0
    search_counter = 0
    click_counter = 0
    for enter_word in enter_actions:
        for app in all_apps:
            for location in locations:
                counter = counter + 1

                if counter % 2 == 0:
                    continue
                if counter % 11 == 0:
                    string = enter_word + " " + location + " from the " + app
                elif counter % 13 == 0:
                    string = enter_word + " " + location + " from a " + app
                elif counter % 15 == 0:
                    string = enter_word + " " + location + " in " + app
                elif counter % 17 == 0:
                    string = enter_word + " " + location + " " + app
                elif counter % 19 == 0:
                    string = enter_word + " " + location + " an " + app
                else:
                    string = enter_word + " " + location + " from " + app
                if counter % 23 == 0:
                    string += " app"
                if counter % 39 == 0:
                    string += " and " + scroll_actions[swipe_counter % len(scroll_actions)]
                    swipe_counter += 1
                if counter % 35 == 0:
                    string += " and " + enter_actions[search_counter % len(enter_actions)]+ " " + locations[search_counter % len(locations)]
                    search_counter += 1
                if counter % 37 == 0:
                    string += " and " + click_actions[click_counter % len(click_actions)] + " " + locations[click_counter % len(locations)]
                    click_counter += 1
                if counter % 33 == 0:
                    string = open_actions[swipe_counter % len(open_actions)] + " " + all_apps[
                        counter % len(all_apps)] + " " + conjunctions[search_counter % len(conjunctions)] + " " + all_apps[
                                 swipe_counter % len(all_apps)] + " "+  conjunctions[counter % len(conjunctions)]+" " + enter_actions[
                                 swipe_counter % len(enter_actions)] + " " + items[
                                 counter % len(items)] + " " +  conjunctions[swipe_counter % len(conjunctions)] + " from " + locations[search_counter % len(locations)]
                outputStr.append(string)
    # plurals
    for enter_word in enter_actions:
        for app in normal_app_names:
            for noun in plurals:
                counter = counter + 1
                if counter % 11 == 0:
                    string = enter_word + " " + noun + " from the " + app
                    outputStr.append(string)
                elif counter % 17 == 0:
                    string = enter_word + " " + noun + " in " + app
                    outputStr.append(string)

    print(len(outputStr))
    return outputStr


def entity_analysis(content, doc):
    content = content.strip()
    data = clean_data2(content)
    if len(data) == 0:
        return
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

def clean_data2(unclean_data):

    data = []

    for word in click_actions:
        word = word.lower()
        if unclean_data.find(word) != -1:
            entity = "Click Action"
            data.append((entity,unclean_data.find(word),unclean_data.find(word)+len(word)))
            # print(unclean_data)
            # print((unclean_data.find(word1)))
            # print(unclean_data.find(word1)+len(word1))


    for word in scroll_actions:
        if unclean_data.find(word) != -1:
            entity = "Scroll Action"
            data.append((entity,unclean_data.find(word),unclean_data.find(word)+len(word)))

    for word in enter_actions:
        if unclean_data.find(word) != -1:
            entity = "Enter Action"
            data.append((entity,unclean_data.find(word),unclean_data.find(word)+len(word)))

    for word in open_actions:
        if unclean_data.find(word) != -1:
            entity = "Open Action"
            data.append((entity,unclean_data.find(word),unclean_data.find(word)+len(word)))

    isRecognized = False
    for word in unnecessary_word_app_names:
        if unclean_data.find(word) != -1:
            entity = "Unnecessary App"
            data.append((entity,unclean_data.find(word),unclean_data.find(word)+len(word)))
            isRecognized = True

    for word in multi_word_apps:
        if unclean_data.find(word) != -1:
            entity = "Multi Word Name"
            data.append((entity,unclean_data.find(word),unclean_data.find(word)+len(word)))
            isRecognized = True

    if isRecognized is False:
        for word in faulty_app_names:
            if unclean_data.find(word) != -1:
                entity = "Faulty App Name"
                data.append((entity,unclean_data.find(word),unclean_data.find(word)+len(word)))
                isRecognized = True

    if isRecognized is False:
        for word in normal_app_names:
            if unclean_data.find(word) != -1:
                entity = "Normal App Name"
                data.append((entity,unclean_data.find(word),unclean_data.find(word)+len(word)))
                isRecognized = True

    for word in plurals:
        if unclean_data.find(word) != -1:
            entity = "Plural"
            data.append((entity, unclean_data.find(word), unclean_data.find(word) + len(word)))

    for word in items:
        if unclean_data.find(word) != -1:
            entity = "Item"
            data.append((entity, unclean_data.find(word), unclean_data.find(word) + len(word)))

    for word in locations:
        if unclean_data.find(word) != -1:
            entity = "Location"
            data.append((entity, unclean_data.find(word), unclean_data.find(word) + len(word)))

    for word in conjunctions:
        if unclean_data.find(word) != -1:
            entity = "Conjunction"
            data.append((entity, unclean_data.find(word), unclean_data.find(word) + len(word)))

    return data


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
        for word1 in click_actions:
            word1 = word1.lower()
            if word == word1:
                entity = "Click Action"
                check1 = True

        check2 = False
        if entity == '':
            for word2 in scroll_actions:
                word2 = word2.lower()
                if word == word2:
                    entity = "Scroll Action"
                    check2 = True

        check3 = False
        if entity == '':
            for word3 in open_actions:
                word3 = word3.lower()
                if word == word3:
                    entity = "Open Action"
                    check3 = True

        check4 = False
        if entity == '':
            for word4 in enter_actions:
                word4 = word4.lower()
                if word == word4:
                    entity = "Enter Action"
                    check4 = True

        check5 = False
        if entity == '':
            for word5 in unnecessary_word_app_names:
                word5 = word5.lower()
                if word == word5:
                    entity = "Unnecessary App Name"
                    check5 = True

        check6 = False
        if entity == '':
            for word6 in multi_word_apps:
                word6 = word6.lower()
                if word == word6:
                    entity = "Multi Word App Name"
                    check6 = True
        check7 = False
        if entity == '':
            for word7 in faulty_app_names:
                word7 = word7.lower()
                if word == word7:
                    entity = "Ambiguous App Name"
                    check7 = True

        check8 = False
        if entity == '':
            for word8 in normal_app_names:
                word8 = word8.lower()
                if word == word8:
                    entity = "Normal App Name"
                    check8 = True

        check9 = False
        if entity == '':
            for word9 in plurals:
                word9 = word9.lower()
                if word == word9:
                    entity = "Plurals Noun"
                    check9 = True

        check10 = False
        if entity == '':
            for word10 in locations:
                word10 = word10.lower()
                if word == word10:
                    entity = "Location"
                    check10 = True
        check11 = False
        if entity == '':
            for word11 in items:
                word11 = word11.lower()
                if word == word11:
                    entity = "Item"
                    check11 = True

        check12 = False
        if entity == '':
            for word12 in conjunctions:
                word12 = word12.lower()
                if word == word12:
                    entity = "Boolean"
                    check12 = True

        if not (check1 or check2 or check3 or check4 or check5 or check6 or check7 or check8 or check9 or check10 or check11 or check12):
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
    # clean_data2('click cinemas from the Swiggy')
