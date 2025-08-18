use std::collections::LinkedList;
use std::collections::HashMap;
use serde::Serialize;

#[derive(Serialize)]
struct Graph {
    adj: HashMap<usize, Vec<usize>>,
}

#[tauri::command]
fn get_graph() -> Graph {
    let mut adj: Vec<LinkedList<usize>> = vec![LinkedList::new(); 5];

    adj[0].push_back(1);
    adj[0].push_back(4);
    adj[1].push_back(0);
    adj[1].push_back(2);
    adj[1].push_back(3);
    adj[2].push_back(1);
    adj[2].push_back(3);
    adj[3].push_back(1);
    adj[3].push_back(2);
    adj[4].push_back(0);
    adj[4].push_back(1);
    adj[4].push_back(3);

    println!("{:?}", adj);

    let mut map = HashMap::new();
    for (i, neighbors) in adj.iter().enumerate() {
        let v: Vec<usize> = neighbors.iter().copied().collect();
        map.insert(i, v);
    }

    Graph { adj: map }
}



#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_opener::init())
        .invoke_handler(tauri::generate_handler![get_graph])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
