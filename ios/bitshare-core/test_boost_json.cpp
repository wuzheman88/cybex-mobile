#include <iostream>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>

using namespace std;
using namespace boost::property_tree;

string test_json_parse(string input)
{
    stringstream str_stream(input);
    ostringstream oss;

    ptree root;
    read_json(str_stream, root);
    
    string method = root.get<string>("method");
    oss << "method:"<< method << ";";

    ptree params_node = root.get_child("params");
    for(ptree::iterator it = params_node.begin(); it != params_node.end(); ++it)
    {
        oss << it->second.get_value<string>() << " ";
    }
    return oss.str();
}

//int main()
//{
//    string input_json = "{\"method\": \"abc\", \"params\": [ \"def\", \"hij\", 0, 3 , \"xxx\" ]}";
//    string method_name = test_json_parse(input_json);
//    cout << method_name << endl;
//    return 0;
//}

